package com.viscript_recipe.recipe.vanilla;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.StonecutterRecipe;

public class ViscriptStonecutterRecipe extends StonecutterRecipe {
    private final boolean showNotification;

    public ViscriptStonecutterRecipe(ResourceLocation recipeId, String group, Ingredient ingredient, ItemStack result, boolean showNotification) {
        super(recipeId, group, ingredient, result);
        this.showNotification = showNotification;
    }

    @Override
    public boolean showNotification() {
        return showNotification;
    }
}
