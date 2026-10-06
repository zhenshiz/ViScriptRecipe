package com.viscript_recipe.mixin;

import com.direwolf20.justdirethings.common.blockentities.basebe.GooBlockBE_Base;
import com.viscript_recipe.compat.justdirethings.JustDireRecipeRuntimeSupport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 清理已放置凝胶的旧查询和计时，避免重载后继续使用旧产物。 */
@Pseudo
@Mixin(targets = "com.direwolf20.justdirethings.common.blockentities.basebe.GooBlockBE_Base", remap = false)
public abstract class JustDireGooRecipeCacheMixin {
    @Unique private long viscriptRecipe$cacheRevision;

    @Inject(method = {"findOutput", "findDuration"}, at = @At("HEAD"), remap = false)
    private void viscriptRecipe$refreshRecipes(CallbackInfoReturnable<?> cir) {
        long revision = JustDireRecipeRuntimeSupport.getRevision();
        if (viscriptRecipe$cacheRevision == revision) return;
        viscriptRecipe$cacheRevision = revision;
        var goo = (GooBlockBE_Base) (Object) this;
        goo.outputCache.clear(); goo.durationCache.clear();
        goo.sidedCounters.replaceAll((side, old) -> -1);
        goo.sidedDurations.replaceAll((side, old) -> -1);
        if (goo.getLevel() != null && !goo.getLevel().isClientSide) goo.markDirtyClient();
    }
}
