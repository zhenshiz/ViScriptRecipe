package com.viscript_recipe.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.stal111.forbidden_arcanus.common.item.crafting.ClibanoRecipe;
import com.stal111.forbidden_arcanus.common.item.crafting.ClibanoRecipeInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.Optional;

@Pseudo
@Mixin(targets = "com.stal111.forbidden_arcanus.common.block.entity.clibano.ClibanoMainBlockEntity$CachedRecipeCheck", remap = false)
public abstract class ForbiddenArcanusClibanoCacheMixin {
    @WrapMethod(method = "checkRecipe")
    private Optional<RecipeHolder<ClibanoRecipe>> viscriptRecipe$resolveCurrentRecipe(
            @Nullable RecipeHolder<ClibanoRecipe> cached, ClibanoRecipeInput input, Level level,
            Operation<Optional<RecipeHolder<ClibanoRecipe>>> original) {
        if (cached == null) return Optional.empty();
        // 已放置的炉子会继续使用同一个缓存；匹配前按 ID 读取当前配方。
        var holder = level.getRecipeManager().byKey(cached.id()).orElse(null);
        if (holder == cached) return original.call(cached, input, level);
        if (holder == null || !(holder.value() instanceof ClibanoRecipe recipe)) return Optional.empty();
        return original.call(new RecipeHolder<>(holder.id(), recipe), input, level);
    }
}
