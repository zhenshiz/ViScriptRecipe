package com.viscript_recipe.mixin;

import com.viscript_recipe.compat.justdirethings.JustDireRecipeRuntimeSupport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 在原生查询前清除流体转化缓存，同时处理此前未命中配方的缓存。 */
@Pseudo
@Mixin(targets = "com.direwolf20.justdirethings.common.events.EntityEvents", remap = false)
public abstract class JustDireFluidRecipeCacheMixin {
    @Unique private static long viscriptRecipe$cacheRevision;
    @Shadow private static void clearCache() { throw new AssertionError(); }

    @Inject(method = "findRecipe", at = @At("HEAD"), remap = false)
    private static void viscriptRecipe$refreshRecipes(CallbackInfoReturnable<?> cir) {
        long revision = JustDireRecipeRuntimeSupport.getRevision();
        if (viscriptRecipe$cacheRevision == revision) return;
        viscriptRecipe$cacheRevision = revision;
        clearCache();
    }
}
