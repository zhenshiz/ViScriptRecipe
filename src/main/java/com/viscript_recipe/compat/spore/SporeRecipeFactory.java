package com.viscript_recipe.compat.spore;

import com.Harbinger.Spore.Recipes.GraftingRecipe;
import com.Harbinger.Spore.Recipes.SurgeryRecipe;
import com.viscript_recipe.compat.spore.data.SporeGraftingRecipeData;
import com.viscript_recipe.compat.spore.data.SporeSurgeryRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public final class SporeRecipeFactory {
    private static final int GRAFTING_INTERNAL_SLOT_COUNT = 25;
    private static final int GRAFTING_FIRST_INPUT_SLOT = 21;

    private SporeRecipeFactory() {
    }

    public static Recipe<?> compileSurgery(SporeSurgeryRecipeData data) {
        var ingredients = compileIngredients(data.getIngredients(), SporeSurgeryRecipeData.INPUT_COUNT);
        if (ingredients.stream().allMatch(Ingredient::isEmpty)) {
            throw new IllegalArgumentException("Spore surgery recipe must contain at least one ingredient");
        }
        return new SurgeryRecipe(ingredients, requireResult(data.getResult(), "Spore surgery result cannot be empty"));
    }

    public static Recipe<?> compileGrafting(SporeGraftingRecipeData data) {
        var inputs = compileIngredients(data.getIngredients(), SporeGraftingRecipeData.INPUT_COUNT);
        for (int i = 0; i < inputs.size(); i++) {
            if (inputs.get(i).isEmpty()) {
                throw new IllegalArgumentException("Spore grafting input " + (i + 1) + " cannot be empty");
            }
        }
        var ingredients = NonNullList.withSize(GRAFTING_INTERNAL_SLOT_COUNT, Ingredient.EMPTY);
        for (int i = 0; i < inputs.size(); i++) {
            ingredients.set(GRAFTING_FIRST_INPUT_SLOT + i, inputs.get(i));
        }
        return new GraftingRecipe(ingredients, requireResult(data.getResult(), "Spore grafting result cannot be empty"));
    }

    private static NonNullList<Ingredient> compileIngredients(List<RecipeIngredient> source, int size) {
        var ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
        for (int i = 0; i < Math.min(size, source.size()); i++) {
            var ingredient = source.get(i);
            ingredients.set(i, ingredient == null ? Ingredient.EMPTY : ingredient.compile());
        }
        return ingredients;
    }

    private static ItemStack requireResult(ItemStack stack, String message) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return stack.copy();
    }
}
