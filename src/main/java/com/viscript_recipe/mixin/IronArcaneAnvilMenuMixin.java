package com.viscript_recipe.mixin;

import com.viscript_recipe.compat.irons_spellbooks.IronArcaneAnvilOverrideManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu", remap = false)
public abstract class IronArcaneAnvilMenuMixin {
    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true, remap = false)
    private void viscriptRecipe$createArcaneAnvilOverrideResult(CallbackInfo ci) {
        var accessor = (ItemCombinerMenuAccessor) this;
        var inputSlots = accessor.viscriptRecipe$getInputSlots();
        var result = IronArcaneAnvilOverrideManager.findResult(inputSlots.getItem(0), inputSlots.getItem(1));
        if (result.isEmpty()) {
            return;
        }
        accessor.viscriptRecipe$getResultSlots().setItem(0, result.get());
        ci.cancel();
    }
}
