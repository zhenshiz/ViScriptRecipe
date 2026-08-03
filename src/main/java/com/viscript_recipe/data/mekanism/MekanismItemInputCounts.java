package com.viscript_recipe.data.mekanism;

import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeIngredient;

public final class MekanismItemInputCounts {
    private MekanismItemInputCounts() {
    }

    public static int amount(RecipeIngredient ingredient, int fallbackAmount) {
        var fallback = Math.max(1, fallbackAmount);
        var itemAmount = firstItemAmount(ingredient);
        if (itemAmount > 1) {
            return itemAmount;
        }
        return Math.max(fallback, itemAmount);
    }

    public static int firstItemAmount(RecipeIngredient ingredient) {
        if (ingredient == null) return 0;
        return ingredient.getKind() == IngredientValueKind.ITEM ? ingredient.getItem().getCount() : 0;
    }
}
