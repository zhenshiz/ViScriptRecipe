package com.viscript_recipe.compat.justdirethings;

import lombok.Getter;

/** 配方重载后通知原生查询缓存失效，在使用缓存的线程上延迟清理。 */
public final class JustDireRecipeRuntimeSupport {
    @Getter private static volatile long revision;
    private JustDireRecipeRuntimeSupport() {}

    /** 推进版本，使已有凝胶和流体转化查询在下次使用前重新读取配方。 */
    public static void invalidateRecipeCaches() { revision++; }
}
