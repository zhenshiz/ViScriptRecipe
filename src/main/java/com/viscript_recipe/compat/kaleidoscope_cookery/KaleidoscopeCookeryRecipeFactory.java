package com.viscript_recipe.compat.kaleidoscope_cookery;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.ChoppingBoardRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.MillstoneRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.SteamerRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.TeapotRecipe;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.kaleidoscope_cookery.KaleidoscopeChoppingBoardRecipeData;
import com.viscript_recipe.data.kaleidoscope_cookery.KaleidoscopeMillstoneRecipeData;
import com.viscript_recipe.data.kaleidoscope_cookery.KaleidoscopePotRecipeData;
import com.viscript_recipe.data.kaleidoscope_cookery.KaleidoscopeSteamerRecipeData;
import com.viscript_recipe.data.kaleidoscope_cookery.KaleidoscopeStockpotRecipeData;
import com.viscript_recipe.data.kaleidoscope_cookery.KaleidoscopeTeapotRecipeData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class KaleidoscopeCookeryRecipeFactory {
    private static final int MAX_INPUTS = 9;
    private static final ResourceLocation DEFAULT_SOUP_BASE = ResourceLocation.withDefaultNamespace("water");
    private static final ResourceLocation DEFAULT_STOCKPOT_COOKING_TEXTURE = kaleidoscope("stockpot/default_cooking");
    private static final ResourceLocation DEFAULT_STOCKPOT_FINISHED_TEXTURE = kaleidoscope("stockpot/default_finished");
    private static final ResourceLocation DEFAULT_CHOPPING_MODEL = kaleidoscope("empty");

    private KaleidoscopeCookeryRecipeFactory() {
    }

    public static Recipe<?> compilePot(KaleidoscopePotRecipeData data) {
        var ingredients = compileIngredients(data.getIngredients());
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Kaleidoscope Cookery pot recipe must have at least one ingredient");
        }
        return new PotRecipe(
                Math.max(1, data.getTime()),
                Math.max(0, data.getStirFryCount()),
                compileOptionalIngredient(data.getCarrier(), Ingredient.EMPTY),
                ingredients,
                requireItem(data.getResult(), "Kaleidoscope Cookery pot result cannot be empty")
        );
    }

    public static Recipe<?> compileStockpot(KaleidoscopeStockpotRecipeData data) {
        var ingredients = compileIngredients(data.getIngredients());
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Kaleidoscope Cookery stockpot recipe must have at least one ingredient");
        }
        return new StockpotRecipe(
                ingredients,
                idOrDefault(data.getSoupBase(), DEFAULT_SOUP_BASE),
                requireItem(data.getResult(), "Kaleidoscope Cookery stockpot result cannot be empty"),
                Math.max(1, data.getTime()),
                compileOptionalIngredient(data.getCarrier(), Ingredient.of(Items.BOWL)),
                idOrDefault(data.getCookingTexture(), DEFAULT_STOCKPOT_COOKING_TEXTURE),
                idOrDefault(data.getFinishedTexture(), DEFAULT_STOCKPOT_FINISHED_TEXTURE),
                data.getCookingBubbleColor(),
                data.getFinishedBubbleColor()
        );
    }

    public static Recipe<?> compileMillstone(KaleidoscopeMillstoneRecipeData data) {
        var ingredient = requireIngredient(data.getIngredient(), "Kaleidoscope Cookery millstone input cannot be empty");
        return new MillstoneRecipe(
                ingredient,
                requireItem(data.getResult(), "Kaleidoscope Cookery millstone result cannot be empty")
        );
    }

    public static Recipe<?> compileChoppingBoard(KaleidoscopeChoppingBoardRecipeData data) {
        var ingredient = requireIngredient(data.getIngredient(), "Kaleidoscope Cookery chopping board input cannot be empty");
        return new ChoppingBoardRecipe(
                ingredient,
                requireItem(data.getResult(), "Kaleidoscope Cookery chopping board result cannot be empty"),
                Math.max(1, data.getCutCount()),
                idOrDefault(data.getModelId(), DEFAULT_CHOPPING_MODEL)
        );
    }

    public static Recipe<?> compileSteamer(KaleidoscopeSteamerRecipeData data) {
        var ingredient = requireIngredient(data.getIngredient(), "Kaleidoscope Cookery steamer input cannot be empty");
        return new SteamerRecipe(
                ingredient,
                requireItem(data.getResult(), "Kaleidoscope Cookery steamer result cannot be empty"),
                Math.max(1, data.getCookTick())
        );
    }

    public static Recipe<?> compileTeapot(KaleidoscopeTeapotRecipeData data) {
        var ingredient = requireIngredient(data.getIngredient(), "Kaleidoscope Cookery teapot input cannot be empty");
        return new TeapotRecipe(
                idOrDefault(data.getTeaFluid(), DEFAULT_SOUP_BASE),
                ingredient,
                Math.max(1, data.getIngredientCount()),
                Math.max(1, data.getTime()),
                requireItem(data.getResult(), "Kaleidoscope Cookery teapot result cannot be empty")
        );
    }

    private static List<Ingredient> compileIngredients(List<RecipeIngredient> ingredients) {
        var compiled = new ArrayList<Ingredient>();
        for (var ingredientData : safeList(ingredients)) {
            if (compiled.size() >= MAX_INPUTS) {
                break;
            }
            var ingredient = compileIngredient(ingredientData);
            if (!ingredient.isEmpty()) {
                compiled.add(ingredient);
            }
        }
        return compiled;
    }

    private static Ingredient requireIngredient(RecipeIngredient ingredient, String message) {
        var compiled = compileIngredient(ingredient);
        if (compiled.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return compiled;
    }

    private static Ingredient compileOptionalIngredient(RecipeIngredient ingredient, Ingredient fallback) {
        var compiled = compileIngredient(ingredient);
        return compiled.isEmpty() ? fallback : compiled;
    }

    private static Ingredient compileIngredient(RecipeIngredient ingredient) {
        return ingredient == null ? Ingredient.EMPTY : ingredient.compile();
    }

    private static ItemStack requireItem(ItemStack stack, String message) {
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            throw new IllegalArgumentException(message);
        }
        var copy = stack.copy();
        copy.setCount(Math.max(1, Math.min(99, copy.getCount())));
        return copy;
    }

    private static ResourceLocation idOrDefault(ResourceLocation id, ResourceLocation fallback) {
        return id == null ? fallback : id;
    }

    private static ResourceLocation kaleidoscope(String path) {
        return ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", path);
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list.stream().filter(Objects::nonNull).toList();
    }
}
