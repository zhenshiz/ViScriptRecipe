package com.viscript_recipe.compat.extendedcrafting;

import com.blakebr0.extendedcrafting.crafting.recipe.*;
import com.viscript_lib.util.math.Clamp;
import com.viscript_recipe.compat.extendedcrafting.data.*;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
import com.viscript_recipe.recipe.vanilla.ShapedRecipePattern;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public final class ExtendedCraftingRecipeFactory {
    private static final int TABLE_MAX_INPUTS = 81;
    private static final int GRID_3X3_INPUTS = 9;
    private static final int COMBINATION_MAX_PEDESTALS = 8;
    private static final int COMPRESSOR_MAX_INPUTS = 8;

    private ExtendedCraftingRecipeFactory() {
    }

    public static Recipe<?> compileTable(ResourceLocation recipeId, ResourceLocation type, ExtendedCraftingTableRecipeData data) {
        var tier = normalizedTableTier(type, data);
        if (ExtendedCraftingRecipeEditorTypes.isShapedTableType(type)) {
            var pattern = compilePattern(data.getPattern(), data.getKey(), data.getWidth(), data.getHeight());
            return new ShapedTableRecipe(recipeId,
                    pattern.width(), pattern.height(), pattern.ingredients(),
                    requireResult(data.getResult(), "Extended Crafting table recipe result cannot be empty"),
                    tier
            );
        }
        if (ExtendedCraftingRecipeEditorTypes.isShapelessTableType(type)) {
            var ingredients = compileIngredients(data.getShapelessIngredients(), TABLE_MAX_INPUTS);
            if (ingredients.isEmpty()) {
                throw new IllegalArgumentException("Extended Crafting shapeless table recipe must have at least one ingredient");
            }
            return new ShapelessTableRecipe(recipeId, ingredients, requireResult(data.getResult(), "Extended Crafting table recipe result cannot be empty"), tier);
        }
        throw new IllegalArgumentException("Unsupported Extended Crafting table recipe type: " + type);
    }

    private static int normalizedTableTier(ResourceLocation type, ExtendedCraftingTableRecipeData data) {
        var tier = data.getTier();
        return tier == 0 ? ExtendedCraftingRecipeEditorTypes.tableTierForType(type) : tier;
    }

    public static Recipe<?> compileEnderCrafter(ResourceLocation recipeId, ResourceLocation type, ExtendedCraftingEnderCrafterRecipeData data) {
        var result = requireResult(data.getResult(), "Extended Crafting ender crafter recipe result cannot be empty");
        var time = Math.max(0, data.getCraftingTime());
        if (ExtendedCraftingRecipeEditorTypes.isShapedEnderType(type)) {
            var pattern = compilePattern(data.getPattern(), data.getKey(), 3, 3);
            return new ShapedEnderCrafterRecipe(recipeId, pattern.width(), pattern.height(), pattern.ingredients(), result, time);
        }
        var ingredients = compileIngredients(data.getShapelessIngredients(), GRID_3X3_INPUTS);
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Extended Crafting shapeless ender recipe must have at least one ingredient");
        }
        return new ShapelessEnderCrafterRecipe(recipeId, ingredients, result, time);
    }

    public static Recipe<?> compileFluxCrafter(ResourceLocation recipeId, ResourceLocation type, ExtendedCraftingFluxCrafterRecipeData data) {
        var result = requireResult(data.getResult(), "Extended Crafting flux crafter recipe result cannot be empty");
        var powerRequired = Math.max(0, data.getPowerRequired());
        var powerRate = Math.max(0, data.getPowerRate());
        if (ExtendedCraftingRecipeEditorTypes.isShapedFluxType(type)) {
            var pattern = compilePattern(data.getPattern(), data.getKey(), 3, 3);
            return new ShapedFluxCrafterRecipe(recipeId, pattern.width(), pattern.height(), pattern.ingredients(), result, powerRequired, powerRate);
        }
        var ingredients = compileIngredients(data.getShapelessIngredients(), GRID_3X3_INPUTS);
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Extended Crafting shapeless flux recipe must have at least one ingredient");
        }
        return new ShapelessFluxCrafterRecipe(recipeId, ingredients, result, powerRequired, powerRate);
    }

    public static Recipe<?> compileCombination(ResourceLocation recipeId, ExtendedCraftingCombinationRecipeData data) {
        // 1.20.1 input 和基座物品合并
        List<RecipeIngredient> items = data.getPedestalItems();
        items.add(0, data.getInput());
        var pedestalItems = compileIngredients(items, COMBINATION_MAX_PEDESTALS + 1);
        if (pedestalItems.isEmpty()) {
            throw new IllegalArgumentException("Extended Crafting combination recipe must have at least one pedestal item");
        }
        return new CombinationRecipe(
                recipeId,
                pedestalItems,
                requireResult(data.getResult(), "Extended Crafting combination recipe result cannot be empty"),
                Math.max(0, data.getPowerCost()),
                Math.max(0, data.getPowerRate())
        );
    }

    public static Recipe<?> compileCompressor(ResourceLocation recipeId, ExtendedCraftingCompressorRecipeData data) {
        RecipeIngredient ingredient = data.getInputs().get(0);
        var input = compileIngredient(ingredient);
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Extended Crafting compressor recipe must have at least one counted input");
        }
        var catalyst = compileIngredient(data.getCatalyst());
        if (catalyst.isEmpty()) {
            throw new IllegalArgumentException("Extended Crafting compressor recipe must have a catalyst");
        }
        return new CompressorRecipe(
                recipeId, input,
                requireResult(data.getResult(), "Extended Crafting compressor recipe result cannot be empty"),
                ingredient.getCount(),
                catalyst,
                Math.max(0, data.getPowerCost()),
                Math.max(0, data.getPowerRate())
        );
    }

    public static Recipe<?> compileUltimateSingularity(ResourceLocation recipeId, ExtendedCraftingUltimateSingularityRecipeData data) {
        return new UltimateSingularityRecipe(recipeId, requireResult(data.getResult(), "Extended Crafting ultimate singularity result cannot be empty"));
    }

    private static ShapedRecipePattern compilePattern(List<String> pattern, List<ShapedKeyEntry> key, int width, int height) {
        var normalizedPattern = normalizePattern(pattern, width, height);
        if (normalizedPattern.stream().allMatch(String::isBlank)) {
            throw new IllegalArgumentException("Extended Crafting shaped recipe pattern cannot be empty");
        }
        var compiledKey = new LinkedHashMap<Character, Ingredient>();
        for (var entry : safeList(key)) {
            compiledKey.put(entry.compileSymbol(), entry.compileIngredient());
        }
        return ShapedRecipePattern.of(compiledKey, normalizedPattern);
    }

    private static List<String> normalizePattern(List<String> pattern, int width, int height) {
        var normalized = new ArrayList<String>();
        var safeWidth = Clamp.clamp(width, 1, 9);
        var safeHeight = Clamp.clamp(height, 1, 9);
        for (int row = 0; row < safeHeight; row++) {
            var line = pattern != null && row < pattern.size() && pattern.get(row) != null ? pattern.get(row) : "";
            if (line.length() > safeWidth) {
                line = line.substring(0, safeWidth);
            }
            normalized.add(line + " ".repeat(safeWidth - line.length()));
        }
        return normalized;
    }

    private static NonNullList<Ingredient> compileIngredients(List<RecipeIngredient> ingredients, int maxCount) {
        var compiled = NonNullList.<Ingredient>create();
        for (var ingredientData : safeList(ingredients)) {
            if (compiled.size() >= maxCount) {
                break;
            }
            var ingredient = compileIngredient(ingredientData);
            if (!ingredient.isEmpty()) {
                compiled.add(ingredient);
            }
        }
        return compiled;
    }

    private static Ingredient compileIngredient(RecipeIngredient ingredient) {
        return ingredient == null ? Ingredient.EMPTY : ingredient.compile();
    }

    private static ItemStack requireResult(ItemStack stack, String message) {
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            throw new IllegalArgumentException(message);
        }
        var copy = stack.copy();
        copy.setCount(Clamp.clamp(copy.getCount(), 1, 99));
        return copy;
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list.stream().filter(Objects::nonNull).toList();
    }
}
