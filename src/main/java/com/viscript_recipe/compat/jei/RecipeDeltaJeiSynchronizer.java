package com.viscript_recipe.compat.jei;

import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.client.RecipeDeltaClientState;
import com.viscript_recipe.compat.jei.create.CreateJeiRecipeFilter;
import com.viscript_recipe.compat.jei.irons_spellbooks.IronSpellbooksJeiRecipeFilter;
import com.viscript_recipe.data.create.CreateProcessingKind;
import com.viscript_recipe.data.create.CreateRecipeEditorTypes;
import com.viscript_recipe.data.irons_spellbooks.IronSpellbooksRecipeEditorTypes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;

/** Applies recipe changes through JEI's public runtime API and performs a JEI-only fallback when needed. */
public final class RecipeDeltaJeiSynchronizer {
    private static final int MAX_HIDDEN_RUNTIME_RECIPES = 256;
    private static final String SYNTHETIC_PREFIX = "jei_delta/";

    private static IJeiRuntime runtime;
    private static long runtimeGeneration;
    private static int hiddenRuntimeRecipes;
    private static boolean forcingFullReload;

    private RecipeDeltaJeiSynchronizer() {
    }

    public static void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        runtimeGeneration++;
        hiddenRuntimeRecipes = 0;
        var state = RecipeDeltaClientState.jeiSyncState();
        if (!state.touchedRecipeIds().isEmpty()) {
            var currentRecipes = currentRecipes(state.touchedRecipeIds());
            if (!canIncrementallyUpdate(jeiRuntime.getRecipeManager(), state.recipeTypeHints())) {
                forceFullJeiReload("unsupported JEI recipe object type after runtime restart");
                return;
            }
            reconcile(
                    jeiRuntime.getRecipeManager(),
                    state.revision(),
                    state.touchedRecipeIds(),
                    currentRecipes,
                    state.recipeTypeHints()
            );
        }
        applyCompatFilters(jeiRuntime);
    }

    public static void onRuntimeUnavailable() {
        runtime = null;
    }

    public static void applyBaseline() {
        var jeiRuntime = runtime;
        if (jeiRuntime != null) {
            applyCompatFilters(jeiRuntime);
        }
    }

    public static void applyDelta(
            long revision,
            Set<ResourceLocation> affectedRecipeIds,
            Map<ResourceLocation, RecipeHolder<?>> oldRecipes,
            Map<ResourceLocation, RecipeHolder<?>> newRecipes,
            Map<ResourceLocation, ResourceLocation> oldEditorTypes,
            Map<ResourceLocation, ResourceLocation> newEditorTypes,
            boolean arcaneAnvilChanged
    ) {
        if (affectedRecipeIds.isEmpty() && !arcaneAnvilChanged) {
            return;
        }
        if (arcaneAnvilChanged || affectsAutomaticBrewing(affectedRecipeIds, oldEditorTypes, newEditorTypes)) {
            forceFullJeiReload(arcaneAnvilChanged
                    ? "Iron's Spells arcane anvil recipes changed"
                    : "Create automatic brewing recipes changed");
            return;
        }

        var jeiRuntime = runtime;
        if (jeiRuntime == null) {
            forceFullJeiReload("JEI runtime is not available");
            return;
        }

        var typeHints = collectTypeHints(affectedRecipeIds, oldRecipes, newRecipes);
        var recipeManager = jeiRuntime.getRecipeManager();
        if (!canIncrementallyUpdate(recipeManager, typeHints)) {
            forceFullJeiReload("a changed JEI category does not use RecipeHolder recipes");
            return;
        }

        reconcile(recipeManager, revision, affectedRecipeIds, newRecipes, typeHints);
        applyCompatFilters(jeiRuntime);
        if (hiddenRuntimeRecipes >= MAX_HIDDEN_RUNTIME_RECIPES) {
            forceFullJeiReload("the hidden JEI recipe history reached its cleanup threshold");
        }
    }

    private static boolean affectsAutomaticBrewing(
            Set<ResourceLocation> affectedRecipeIds,
            Map<ResourceLocation, ResourceLocation> oldEditorTypes,
            Map<ResourceLocation, ResourceLocation> newEditorTypes
    ) {
        var automaticBrewing = CreateProcessingKind.AUTOMATIC_BREWING.typeId();
        for (var id : affectedRecipeIds) {
            if (automaticBrewing.equals(oldEditorTypes.get(id)) || automaticBrewing.equals(newEditorTypes.get(id))) {
                return true;
            }
        }
        return false;
    }

    private static Map<ResourceLocation, Set<ResourceLocation>> collectTypeHints(
            Set<ResourceLocation> affectedRecipeIds,
            Map<ResourceLocation, RecipeHolder<?>> oldRecipes,
            Map<ResourceLocation, RecipeHolder<?>> newRecipes
    ) {
        var hints = new LinkedHashMap<ResourceLocation, Set<ResourceLocation>>();
        for (var id : affectedRecipeIds) {
            var values = new LinkedHashSet<ResourceLocation>();
            addTypeHint(values, oldRecipes.get(id));
            addTypeHint(values, newRecipes.get(id));
            hints.put(id, values);
        }
        return hints;
    }

    private static void addTypeHint(Set<ResourceLocation> hints, RecipeHolder<?> holder) {
        if (holder == null) {
            return;
        }
        var typeId = BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType());
        if (typeId != null) {
            hints.add(typeId);
        }
    }

    private static boolean canIncrementallyUpdate(
            IRecipeManager recipeManager,
            Map<ResourceLocation, Set<ResourceLocation>> typeHints
    ) {
        var uniqueTypes = new LinkedHashSet<ResourceLocation>();
        typeHints.values().forEach(uniqueTypes::addAll);
        for (var typeId : uniqueTypes) {
            var jeiType = recipeManager.getRecipeType(typeId).orElse(null);
            if (jeiType != null && !RecipeHolder.class.isAssignableFrom(jeiType.getRecipeClass())) {
                return false;
            }
        }
        return true;
    }

    private static void reconcile(
            IRecipeManager recipeManager,
            long revision,
            Set<ResourceLocation> affectedRecipeIds,
            Map<ResourceLocation, RecipeHolder<?>> desiredRecipes,
            Map<ResourceLocation, Set<ResourceLocation>> typeHints
    ) {
        var recipesByType = new LinkedHashMap<ResourceLocation, ArrayList<RecipeHolder<?>>>();
        for (var holder : desiredRecipes.values()) {
            var typeId = BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType());
            if (typeId != null) {
                recipesByType.computeIfAbsent(typeId, ignored -> new ArrayList<>()).add(holder);
            }
        }

        var allTypes = new LinkedHashSet<ResourceLocation>();
        typeHints.values().forEach(allTypes::addAll);
        allTypes.addAll(recipesByType.keySet());
        for (var typeId : allTypes) {
            var jeiType = recipeManager.getRecipeType(typeId).orElse(null);
            if (jeiType == null) {
                continue;
            }
            reconcileType(
                    recipeManager,
                    jeiType,
                    revision,
                    affectedRecipeIds,
                    recipesByType.getOrDefault(typeId, new ArrayList<>())
            );
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void reconcileType(
            IRecipeManager recipeManager,
            RecipeType jeiType,
            long revision,
            Set<ResourceLocation> affectedRecipeIds,
            Collection<RecipeHolder<?>> desiredRecipes
    ) {
        var existing = recipeManager.createRecipeLookup(jeiType)
                .includeHidden()
                .get()
                .toList();
        var stale = new ArrayList<>();
        for (var candidate : existing) {
            if (candidate instanceof RecipeHolder<?> holder) {
                var canonicalId = canonicalRecipeId(holder.id());
                if (affectedRecipeIds.contains(canonicalId)) {
                    stale.add(candidate);
                }
            }
        }
        if (!stale.isEmpty()) {
            recipeManager.hideRecipes(jeiType, stale);
            hiddenRuntimeRecipes += stale.size();
        }

        var additions = new ArrayList<RecipeHolder<?>>();
        for (var holder : desiredRecipes) {
            additions.add(new RecipeHolder<>(syntheticRecipeId(holder.id(), revision), holder.value()));
        }
        if (!additions.isEmpty()) {
            recipeManager.addRecipes(jeiType, additions);
        }
    }

    private static ResourceLocation syntheticRecipeId(ResourceLocation canonicalId, long revision) {
        return ResourceLocation.fromNamespaceAndPath(
                ViScriptRecipe.MOD_ID,
                SYNTHETIC_PREFIX
                        + runtimeGeneration + "/"
                        + revision + "/"
                        + canonicalId.getNamespace() + "/"
                        + canonicalId.getPath()
        );
    }

    private static ResourceLocation canonicalRecipeId(ResourceLocation displayedId) {
        if (!ViScriptRecipe.MOD_ID.equals(displayedId.getNamespace())
                || !displayedId.getPath().startsWith(SYNTHETIC_PREFIX)) {
            return displayedId;
        }
        var parts = displayedId.getPath().split("/", 5);
        if (parts.length != 5) {
            return displayedId;
        }
        var canonical = ResourceLocation.tryParse(parts[3] + ":" + parts[4]);
        return canonical == null ? displayedId : canonical;
    }

    private static Map<ResourceLocation, RecipeHolder<?>> currentRecipes(Set<ResourceLocation> ids) {
        var level = Minecraft.getInstance().level;
        var recipes = new LinkedHashMap<ResourceLocation, RecipeHolder<?>>();
        if (level == null) {
            return recipes;
        }
        for (var id : ids) {
            level.getRecipeManager().byKey(id).ifPresent(holder -> recipes.put(id, holder));
        }
        return recipes;
    }

    private static void applyCompatFilters(IJeiRuntime jeiRuntime) {
        if (ViScriptRecipe.isModLoaded(CreateRecipeEditorTypes.MOD_ID)) {
            CreateJeiRecipeFilter.apply(jeiRuntime, JeiShowcaseModeState.isShowcaseOnly());
        }
        if (ViScriptRecipe.isModLoaded(IronSpellbooksRecipeEditorTypes.MOD_ID)) {
            IronSpellbooksJeiRecipeFilter.apply(jeiRuntime, JeiShowcaseModeState.isShowcaseOnly());
        }
    }

    private static void forceFullJeiReload(String reason) {
        if (forcingFullReload) {
            return;
        }
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        forcingFullReload = true;
        hiddenRuntimeRecipes = 0;
        ViScriptRecipe.LOGGER.info("Falling back to a JEI-only recipe rebuild because {}", reason);
        try {
            RecipeDeltaClientState.runAsLocalJeiReload(
                    () -> NeoForge.EVENT_BUS.post(new RecipesUpdatedEvent(level.getRecipeManager()))
            );
        } catch (RuntimeException | LinkageError e) {
            ViScriptRecipe.LOGGER.warn("Failed to rebuild JEI after an incremental recipe update", e);
        } finally {
            forcingFullReload = false;
        }
    }
}
