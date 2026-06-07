package com.viscript_recipe.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SmithingTransformRecipe.class)
public interface SmithingTransformRecipeAccessor {
    @Accessor("template")
    Ingredient viscriptRecipe$getTemplate();

    @Accessor("base")
    Ingredient viscriptRecipe$getBase();

    @Accessor("addition")
    Ingredient viscriptRecipe$getAddition();

    @Accessor("result")
    ItemStack viscriptRecipe$getResult();
}
