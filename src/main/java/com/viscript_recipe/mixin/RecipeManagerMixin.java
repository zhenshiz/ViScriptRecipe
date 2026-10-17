package com.viscript_recipe.mixin;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.viscript_recipe.recipe.RecipeOverrideManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 900)
public abstract class RecipeManagerMixin {
    @Shadow
    @Final
    private HolderLookup.Provider registries;

    @WrapMethod(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
    private void viscriptRecipe$applyOverrides(Map<?, JsonElement> map, ResourceManager resourceManager,
                                              ProfilerFiller profiler, Operation<Void> original) {
        // 等待其它模组的加载回调全部结束，再保存基线和应用 VSR 修改。
        original.call(map, resourceManager, profiler);
        RecipeOverrideManager.apply((RecipeManager) (Object) this, registries, resourceManager);
    }
}
