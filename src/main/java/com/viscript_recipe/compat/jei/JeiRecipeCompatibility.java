package com.viscript_recipe.compat.jei;

import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/** 保存已加载模块的 JEI 分类映射和刷新处理。 */
public final class JeiRecipeCompatibility {
    private static final List<Function<Delta, String>> RELOAD_REASONS = new ArrayList<>();
    private static final List<Predicate<ResourceLocation>> REBUILD_TYPES = new ArrayList<>();
    private static final List<Function<ResourceLocation, ResourceLocation>> CATEGORY_MAPPINGS = new ArrayList<>();
    private static final List<BiConsumer<IJeiRuntime, Boolean>> SHOWCASE_FILTERS = new ArrayList<>();
    private static final List<Runnable> RUNTIME_RESET = new ArrayList<>();
    private static final Map<ResourceLocation, CategoryUpdater> CATEGORY_UPDATERS = new HashMap<>();

    private JeiRecipeCompatibility() {}

    public static boolean requiresRebuild(ResourceLocation nativeType) {
        return REBUILD_TYPES.stream().anyMatch(test -> test.test(nativeType));
    }

    public static @Nullable String rebuildReason(Delta delta) {
        for (var reason : RELOAD_REASONS) {
            var result = reason.apply(delta);
            if (result != null) return result;
        }
        return null;
    }

    public static ResourceLocation categoryId(ResourceLocation nativeType) {
        for (var mapping : CATEGORY_MAPPINGS) {
            var category = mapping.apply(nativeType);
            if (category != null) return category;
        }
        return nativeType;
    }

    public static @Nullable CategoryUpdater categoryUpdater(ResourceLocation nativeType) {
        return CATEGORY_UPDATERS.get(nativeType);
    }

    public static void resetRuntime() {
        RUNTIME_RESET.forEach(Runnable::run);
    }

    public static void applyShowcaseFilters(IJeiRuntime runtime, boolean showcaseOnly) {
        SHOWCASE_FILTERS.forEach(filter -> filter.accept(runtime, showcaseOnly));
    }

    public record Delta(
            RecipeManager recipeManager,
            Set<ResourceLocation> affectedIds,
            Map<ResourceLocation, RecipeHolder<?>> oldRecipes,
            Map<ResourceLocation, RecipeHolder<?>> newRecipes,
            Map<ResourceLocation, ResourceLocation> oldEditorTypes,
            Map<ResourceLocation, ResourceLocation> newEditorTypes
    ) {}

    @FunctionalInterface
    public interface CategoryUpdater {
        int reconcile(IRecipeManager manager, RecipeType<?> category, ResourceLocation nativeType,
                      Set<ResourceLocation> affectedIds, Collection<RecipeHolder<?>> desiredRecipes);
    }

    public static final class Registration {
        /**
         * 注册根据增量变化决定是否重建 JEI 的处理。
         *
         * @param reason 返回重建原因的函数，无需重建时返回 <code>null</code>
         */
        public void rebuildWhen(Function<Delta, @Nullable String> reason) {
            RELOAD_REASONS.add(reason);
        }

        /**
         * 指定需要通过完整重建更新的原生配方类型。
         *
         * @param filter 匹配原生配方类型 ID 的条件
         */
        public void rebuildTypes(Predicate<ResourceLocation> filter) {
            REBUILD_TYPES.add(filter);
        }

        /**
         * 声明与原生类型 ID 不同的 JEI 分类 ID。
         *
         * @param mapping 返回目标分类 ID 的函数，不适用时返回 <code>null</code>
         */
        public void mapCategory(Function<ResourceLocation, @Nullable ResourceLocation> mapping) {
            CATEGORY_MAPPINGS.add(mapping);
        }

        /**
         * 注册处理特定分类的增量更新器。
         *
         * @param nativeType 更新器适用的原生配方类型 ID
         * @param updater 根据待更新 ID 和配方更新分类并返回隐藏条目数的处理
         */
        public void updateCategory(ResourceLocation nativeType, CategoryUpdater updater) {
            CATEGORY_UPDATERS.put(nativeType, updater);
        }

        /**
         * 注册在 JEI 运行时可用时重建展示状态的处理。
         *
         * @param reset 初始化当前 JEI 运行时状态的处理
         */
        public void resetRuntime(Runnable reset) {
            RUNTIME_RESET.add(reset);
        }

        /**
         * 注册随 VSR 展示模式更新原生 JEI 分类的过滤器。
         *
         * @param filter 接收当前 JEI 运行时和展示模式开关的处理
         */
        public void showcaseFilter(BiConsumer<IJeiRuntime, Boolean> filter) {
            SHOWCASE_FILTERS.add(filter);
        }
    }
}
