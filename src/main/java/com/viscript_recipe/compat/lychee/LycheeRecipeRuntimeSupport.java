package com.viscript_recipe.compat.lychee;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.util.KUtil;
import snownee.lychee.RecipeTypes;

/** 在增量重载后调用 Lychee 原生缓存构建，覆盖方块索引、实体索引和随机刻状态。 */
public final class LycheeRecipeRuntimeSupport {
    private LycheeRecipeRuntimeSupport() {}

    public static void refreshCaches(RecipeManager recipeManager) {
        // Kiwi 的 replaceRecipes 钩子维护当前管理器；与 Lychee 的原生缓存构建保持同一数据源。
        // 这里只用于 VSR 重载和增量同步；数据包加载由 Lychee 的 updateRegistryTags 钩子处理。
        if (KUtil.getRecipeManager() != recipeManager) return;
        RecipeTypes.buildCache();
        Blocks.rebuildCache();
    }
}
