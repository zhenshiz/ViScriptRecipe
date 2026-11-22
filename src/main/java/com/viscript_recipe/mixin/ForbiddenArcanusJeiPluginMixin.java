package com.viscript_recipe.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.Ritual;
import com.viscript_recipe.compat.forbidden_arcanus.ForgeRitualRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

@Pseudo
@Mixin(targets = "com.stal111.forbidden_arcanus.common.integration.ForbiddenArcanusJEIPlugin", remap = false)
public abstract class ForbiddenArcanusJeiPluginMixin {
    @WrapOperation(method = "registerRecipes", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/Registry;stream()Ljava/util/stream/Stream;"))
    private Stream<Ritual> viscriptRecipe$jeiRituals(Registry<Ritual> registry, Operation<Stream<Ritual>> original) {
        var level = Minecraft.getInstance().level;
        return level == null ? original.call(registry) : ForgeRitualRecipes.visibleRituals(level.getRecipeManager());
    }
}
