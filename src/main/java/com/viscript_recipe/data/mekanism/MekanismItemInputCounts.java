package com.viscript_recipe.data.mekanism;

import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeIngredientValue;
import net.minecraft.world.item.ItemStack;

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
        if (ingredient == null) {
            return 0;
        }
        for (var value : ingredient.getValues()) {
            if (value != null
                    && value.getKind() == IngredientValueKind.ITEM
                    && value.getItem() != null
                    && !value.getItem().isEmpty()) {
                return Math.max(1, value.getItem().getCount());
            }
        }
        return 0;
    }

    public static RecipeIngredient item(ItemStack stack) {
        var ingredient = new RecipeIngredient();
        if (stack == null || stack.isEmpty()) {
            return ingredient;
        }
        var copy = stack.copy();
        copy.setCount(Math.max(1, copy.getCount()));
        ingredient.getValues().add(new RecipeIngredientValue()
                .setKind(IngredientValueKind.ITEM)
                .setItem(copy));
        return ingredient;
    }

    public static RecipeIngredient copyWithItemAmount(RecipeIngredient ingredient, int amount) {
        var copy = new RecipeIngredient();
        if (ingredient == null) {
            return copy;
        }
        var normalizedAmount = Math.max(1, amount);
        for (var value : ingredient.getValues()) {
            if (value == null) {
                continue;
            }
            var valueCopy = new RecipeIngredientValue()
                    .setKind(value.getKind())
                    .setTag(value.getTag())
                    .setItemAbility(value.getItemAbility());
            if (value.getKind() == IngredientValueKind.ITEM && value.getItem() != null) {
                valueCopy.setItem(value.getItem().copyWithCount(normalizedAmount));
            } else if (value.getItem() != null) {
                valueCopy.setItem(value.getItem().copy());
            }
            copy.getValues().add(valueCopy);
        }
        return copy;
    }
}
