package com.viscript_recipe.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.lowdragmc.lowdraglib2.Platform;
import com.viscript_recipe.recipe.RecipeOverrideManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 900)
public abstract class RecipeManagerMixin {
    @WrapMethod(method = "apply*")
    private void viscriptRecipe$applyOverrides(Map<?, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler, Operation<Void> original) {
        original.call(object, resourceManager, profiler);
        RecipeOverrideManager.apply((RecipeManager) (Object) this, Platform.getFrozenRegistry(), resourceManager);
    }
}
