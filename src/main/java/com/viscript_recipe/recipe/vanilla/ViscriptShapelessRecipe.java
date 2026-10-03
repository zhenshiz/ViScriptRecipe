package com.viscript_recipe.recipe.vanilla;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public class ViscriptShapelessRecipe extends ShapelessRecipe {
    private final boolean showNotification;

    public ViscriptShapelessRecipe(ResourceLocation recipeId, String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients, boolean showNotification) {
        super(recipeId, group, category, result, ingredients);
        this.showNotification = showNotification;
    }

    @Override
    public boolean showNotification() {
        return showNotification;
    }
}
