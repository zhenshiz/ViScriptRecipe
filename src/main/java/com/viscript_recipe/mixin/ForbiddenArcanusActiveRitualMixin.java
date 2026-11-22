package com.viscript_recipe.mixin;

import com.viscript_recipe.compat.forbidden_arcanus.ForgeRitualRecipes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.ActiveRitualData", remap = false)
public abstract class ForbiddenArcanusActiveRitualMixin implements ForgeRitualRecipes.ActiveId {
    @Unique @Nullable private ResourceLocation viscriptRecipe$ritualId;

    @Override
    public @Nullable ResourceLocation viscriptRecipe$getRitualId() {return viscriptRecipe$ritualId;}

    @Override
    public void viscriptRecipe$setRitualId(@Nullable ResourceLocation id) {viscriptRecipe$ritualId = id;}

    @Inject(method = "getRitualId", at = @At("HEAD"), cancellable = true)
    private void viscriptRecipe$getRitualId(CallbackInfoReturnable<ResourceLocation> cir) {
        if (viscriptRecipe$ritualId != null) cir.setReturnValue(viscriptRecipe$ritualId);
    }
}
