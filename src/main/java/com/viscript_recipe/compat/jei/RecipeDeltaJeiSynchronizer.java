package com.viscript_recipe.compat.jei;

import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.client.RecipeDeltaClientState;
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

public final class RecipeDeltaJeiSynchronizer {
    private static final int MAX_HIDDEN_RUNTIME_RECIPES = 256;
    private static final String SYNTHETIC_PREFIX = "jei_delta/";

    private static IJeiRuntime runtime;
    private static long runtimeGeneration;
    private static int hiddenRuntimeRecipes;
    private static boolean forcingFullReload;
    /**
     * 增量包可能早于 JEI 运行时和配置初始化完成；回退请求需等运行时可用后再执行，
     * 避免向尚未初始化完成的 JEI 发送 RecipesUpdatedEvent。
     */
    private static String pendingFullReloadReason;

    private RecipeDeltaJeiSynchronizer() {
    }

    public static void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        if (jeiRuntime == null) {
            return;
        }
        runtime = jeiRuntime;
        runtimeGeneration++;
        hiddenRuntimeRecipes = 0;
        JeiRecipeCompatibility.resetRuntime();
        if (tryApplyPendingFullReload()) {
            applyCompatFiltersSafely(jeiRuntime);
            return;
        }
        var state = RecipeDeltaClientState.jeiSyncState();
        try {
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
        } catch (RuntimeException | LinkageError e) {
            ViScriptRecipe.LOGGER.warn("Failed to reconcile recipes when JEI became available", e);
            forceFullJeiReload("JEI rejected the recipe state during runtime initialization");
        }
        applyCompatFiltersSafely(jeiRuntime);
    }

    public static void onRuntimeUnavailable() {
        runtime = null;
    }

    public static void applyBaseline() {
        var jeiRuntime = runtime;
        if (jeiRuntime != null) {
            if (!tryApplyPendingFullReload()) {
                applyCompatFiltersSafely(jeiRuntime);
            }
        }
    }

    public static void applyDelta(
            long revision,
            Set<ResourceLocation> affectedRecipeIds,
            Map<ResourceLocation, RecipeHolder<?>> oldRecipes,
            Map<ResourceLocation, RecipeHolder<?>> newRecipes,
            Map<ResourceLocation, ResourceLocation> oldEditorTypes,
            Map<ResourceLocation, ResourceLocation> newEditorTypes
    ) {
        if (affectedRecipeIds.isEmpty()) {
            return;
        }
        if (runtime == null) {
            forceFullJeiReload("JEI runtime is not available yet");
            return;
        }
        var jeiRuntime = runtime;
        if (tryApplyPendingFullReload()) {
            return;
        }

        try {
            var level = Minecraft.getInstance().level;
            if (level != null) {
                var reason = JeiRecipeCompatibility.rebuildReason(new JeiRecipeCompatibility.Delta(level.getRecipeManager(),
                        affectedRecipeIds, oldRecipes, newRecipes, oldEditorTypes, newEditorTypes));
                if (reason != null) {
                    forceFullJeiReload(reason);
                    return;
                }
            }

            var typeHints = collectTypeHints(affectedRecipeIds, oldRecipes, newRecipes);
            var recipeManager = jeiRuntime.getRecipeManager();
            if (!canIncrementallyUpdate(recipeManager, typeHints)) {
                forceFullJeiReload("a changed JEI category does not use RecipeHolder recipes");
                return;
            }

            reconcile(recipeManager, revision, affectedRecipeIds, newRecipes, typeHints);
            applyCompatFiltersSafely(jeiRuntime);
            if (hiddenRuntimeRecipes >= MAX_HIDDEN_RUNTIME_RECIPES) {
                forceFullJeiReload("the hidden JEI recipe history reached its cleanup threshold");
            }
        } catch (RuntimeException | LinkageError e) {
            // JEI 重建期间可能拒绝分类更新；增量操作失败时，等运行时可用后再完整重建 JEI，避免客户端崩溃。
            ViScriptRecipe.LOGGER.warn("Failed to apply an incremental JEI recipe update", e);
            forceFullJeiReload("JEI rejected an incremental recipe update");
        }
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
            if (JeiRecipeCompatibility.requiresRebuild(typeId)) return false;
            if (JeiRecipeCompatibility.categoryUpdater(typeId) != null) continue;
            var jeiType = recipeManager.getRecipeType(JeiRecipeCompatibility.categoryId(typeId)).orElse(null);
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
        var recipesByType = new LinkedHashMap<ResourceLocation, List<RecipeHolder<?>>>();
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
            var jeiType = recipeManager.getRecipeType(JeiRecipeCompatibility.categoryId(typeId)).orElse(null);
            if (jeiType == null) {
                continue;
            }
            var recipes = recipesByType.getOrDefault(typeId, List.of());
            var updater = JeiRecipeCompatibility.categoryUpdater(typeId);
            if (updater == null) {
                reconcileType(recipeManager, jeiType, revision, affectedRecipeIds, recipes);
            } else {
                hiddenRuntimeRecipes += updater.reconcile(recipeManager, jeiType, typeId, affectedRecipeIds, recipes);
            }
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

    private static void applyCompatFiltersSafely(IJeiRuntime jeiRuntime) {
        try {
            JeiRecipeCompatibility.applyShowcaseFilters(jeiRuntime, JeiShowcaseModeState.isShowcaseOnly());
        } catch (RuntimeException | LinkageError e) {
            ViScriptRecipe.LOGGER.warn("Failed to apply JEI compatibility filters", e);
            forceFullJeiReload("a JEI compatibility filter failed during initialization");
        }
    }

    private static boolean tryApplyPendingFullReload() {
        if (pendingFullReloadReason == null
                || runtime == null
                || Minecraft.getInstance().level == null) {
            return false;
        }
        var reason = pendingFullReloadReason;
        pendingFullReloadReason = null;
        forceFullJeiReload(reason);
        return true;
    }

    private static void forceFullJeiReload(String reason) {
        if (forcingFullReload) {
            return;
        }
        var level = Minecraft.getInstance().level;
        if (runtime == null || level == null) {
            if (pendingFullReloadReason == null) {
                pendingFullReloadReason = reason;
            }
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
