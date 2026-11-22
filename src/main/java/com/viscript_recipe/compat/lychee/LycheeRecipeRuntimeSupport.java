package com.viscript_recipe.compat.lychee;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import snownee.lychee.RecipeTypes;

/** 在增量重载后调用 Lychee 原生缓存构建，覆盖方块索引、实体索引和随机刻状态。 */
public final class LycheeRecipeRuntimeSupport {
    private LycheeRecipeRuntimeSupport() {}

    public static void refreshCaches(RecipeManager recipeManager) {
        // 单人游戏共用静态缓存，和 Lychee 原生钩子一样仅由内置服务器刷新。
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null && server.getRecipeManager() != recipeManager) return;
        RecipeTypes.buildCache(recipeManager);
        Blocks.rebuildCache();
    }
}
