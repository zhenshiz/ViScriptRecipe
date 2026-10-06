package com.viscript_recipe.compat.jei;

import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.client.RecipeDeltaClientState;
import com.viscript_recipe.compat.create.CreateRecipeEditorTypes;
import com.viscript_recipe.compat.create.data.CreateProcessingKind;
import com.viscript_recipe.compat.farm_and_charm.FarmCharmRecipeKind;
import com.viscript_recipe.compat.irons_spellbooks.IronSpellbooksRecipeEditorTypes;
import com.viscript_recipe.compat.jei.create.CreateJeiRecipeFilter;
import com.viscript_recipe.compat.jei.irons_spellbooks.IronSpellbooksJeiRecipeFilter;
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
        FarmCharmJeiRecipeSynchronizer.reset();
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
            Map<ResourceLocation, ResourceLocation> newEditorTypes,
            boolean arcaneAnvilChanged
    ) {
        if (affectedRecipeIds.isEmpty() && !arcaneAnvilChanged) {
            return;
        }
        if (runtime == null) {
            forceFullJeiReload("JEI runtime is not available yet");
            return;
        }
        if (arcaneAnvilChanged || affectsAutomaticBrewing(affectedRecipeIds, oldEditorTypes, newEditorTypes)) {
            forceFullJeiReload(arcaneAnvilChanged
                    ? "Iron's Spells arcane anvil recipes changed"
                    : "Create automatic brewing recipes changed");
            return;
        }

        var jeiRuntime = runtime;
        if (tryApplyPendingFullReload()) {
            return;
        }

        try {
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
            // Eidolon 的 rituals 分类合并了五种原生仪式且直接持有配方；需完整重建 JEI，才能更新没有原生 UID 的分类。
            if ("eidolon_repraised".equals(typeId.getNamespace())) return false;
            // 附魔配方在 JEI 中按等级展开到 enchanter 分类，需重建全部等级的包装配方。
            if (typeId.equals(ResourceLocation.parse("enderio:enchanting"))) return false;
            // TACZ 按枪包工作台、页签和过滤器动态拆分 JEI 分类，原生类型没有一一对应的 JEI UID。
            if (typeId.equals(ResourceLocation.parse("tacz:gun_smith_table_crafting"))) return false;
            // 黏液和流体转化使用不同的 JEI UID，并直接持有配方对象；重建以同步增删改。
            if ("justdirethings".equals(typeId.getNamespace())) return false;
            var farmCharm = FarmCharmRecipeKind.byType(typeId);
            var jeiType = recipeManager.getRecipeType(farmCharm.map(FarmCharmRecipeKind::jeiTypeId).orElse(typeId)).orElse(null);
            if (farmCharm.isPresent()) continue;
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
            var farmCharm = FarmCharmRecipeKind.byType(typeId);
            var jeiType = recipeManager.getRecipeType(farmCharm.map(FarmCharmRecipeKind::jeiTypeId).orElse(typeId)).orElse(null);
            if (jeiType == null) {
                continue;
            }
            if (farmCharm.isPresent()) {
                hiddenRuntimeRecipes += FarmCharmJeiRecipeSynchronizer.reconcile(recipeManager, jeiType, typeId,
                        affectedRecipeIds, recipesByType.getOrDefault(typeId, new ArrayList<>()));
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

    private static void applyCompatFiltersSafely(IJeiRuntime jeiRuntime) {
        try {
            applyCompatFilters(jeiRuntime);
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
