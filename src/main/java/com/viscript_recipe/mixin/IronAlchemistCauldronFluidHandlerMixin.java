package com.viscript_recipe.mixin;

import com.viscript_recipe.compat.irons_spellbooks.IronAlchemistCauldronFluidSupport;
import io.redspace.ironsspellbooks.block.alchemist_cauldron.AlchemistCauldronTile;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AlchemistCauldronTile.AlchemistCauldronFluidHandler.class)
public abstract class IronAlchemistCauldronFluidHandlerMixin {
    @Redirect(
            method = "fill",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/Fluid;is(Lnet/minecraft/tags/TagKey;)Z"
            )
    )
    private boolean viscriptRecipe$allowRecipeFluidInAlchemistCauldron(Fluid instance, TagKey<Fluid> tag) {
        return instance.is(tag) && !IronAlchemistCauldronFluidSupport.allows(instance);
    }
}
