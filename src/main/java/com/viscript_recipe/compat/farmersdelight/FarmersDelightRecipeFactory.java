package com.viscript_recipe.compat.farmersdelight;

import com.viscript_recipe.compat.farmersdelight.data.FarmerCookingPotRecipeData;
import com.viscript_recipe.compat.farmersdelight.data.FarmerCuttingRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.ItemAbility;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.ingredient.ChanceResult;
import vectorwing.farmersdelight.common.crafting.ingredient.ItemAbilityIngredient;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class FarmersDelightRecipeFactory {
    private FarmersDelightRecipeFactory() {
    }

    public static Recipe<?> compileCooking(FarmerCookingPotRecipeData data) {
        var ingredients = NonNullList.<Ingredient>create();
        for (var ingredientData : safeList(data.getIngredients())) {
            var ingredient = compileIngredient(ingredientData);
            if (!ingredient.isEmpty()) {
                ingredients.add(ingredient);
            }
        }
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Farmer's Delight cooking pot recipe must have at least one ingredient");
        }
        if (ingredients.size() > CookingPotRecipe.INPUT_SLOTS) {
            throw new IllegalArgumentException("Farmer's Delight cooking pot recipe cannot have more than 6 ingredients");
        }
        return new CookingPotRecipe(
                "",
                null,
                ingredients,
                requireItem(data.getResult(), "Farmer's Delight cooking pot result cannot be empty"),
                data.getContainer() == null ? ItemStack.EMPTY : data.getContainer().copy(),
                Math.max(0, data.getExperience()),
                Math.max(1, data.getCookingTime())
        );
    }

    public static Recipe<?> compileCutting(FarmerCuttingRecipeData data) {
        var input = compileIngredient(data.getInput());
        var tool = compileIngredient(data.getTool());
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Farmer's Delight cutting board input cannot be empty");
        }
        if (tool.isEmpty()) {
            throw new IllegalArgumentException("Farmer's Delight cutting board tool cannot be empty");
        }
        var results = NonNullList.<ChanceResult>create();
        for (var resultData : safeList(data.getResults())) {
            var stack = resultData == null ? ItemStack.EMPTY : resultData.getItem();
            if (stack != null && !stack.isEmpty()) {
                results.add(new ChanceResult(stack.copy(), Math.clamp(resultData.getChance(), 0, 1)));
            }
        }
        if (results.isEmpty()) {
            throw new IllegalArgumentException("Farmer's Delight cutting board recipe must have at least one result");
        }
        if (results.size() > CuttingBoardRecipe.MAX_RESULTS) {
            throw new IllegalArgumentException("Farmer's Delight cutting board recipe cannot have more than 4 results");
        }
        return new CuttingBoardRecipe("", input, tool, results, sound(data));
    }

    public static Ingredient compileItemAbilityIngredient(String itemAbility) {
        var name = itemAbility == null || itemAbility.isBlank() ? "knife_dig" : itemAbility;
        return new ItemAbilityIngredient(ItemAbility.get(name)).toVanilla();
    }

    private static Ingredient compileIngredient(RecipeIngredient ingredient) {
        return ingredient == null ? Ingredient.EMPTY : ingredient.compile();
    }

    private static ItemStack requireItem(ItemStack stack, String message) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return stack.copy();
    }

    private static Optional<SoundEvent> sound(FarmerCuttingRecipeData data) {
        if (!data.isCustomSound() || data.getSound() == null) {
            return Optional.empty();
        }
        return BuiltInRegistries.SOUND_EVENT.getOptional(data.getSound());
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list.stream().filter(Objects::nonNull).toList();
    }
}
