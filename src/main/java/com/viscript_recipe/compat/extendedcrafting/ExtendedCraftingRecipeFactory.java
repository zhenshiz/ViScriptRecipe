package com.viscript_recipe.compat.extendedcrafting;

import com.blakebr0.cucumber.crafting.ingredient.IngredientWithCount;
import com.blakebr0.extendedcrafting.crafting.recipe.*;
import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeIngredientValue;
import com.viscript_recipe.data.extendedcrafting.*;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

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

    public static Recipe<?> compileTable(ResourceLocation type, ExtendedCraftingTableRecipeData data) {
        var tier = normalizedTableTier(type, data);
        if (ExtendedCraftingRecipeEditorTypes.isShapedTableType(type)) {
            return new ShapedTableRecipe(
                    compilePattern(data.getPattern(), data.getKey(), data.normalizedWidth(), data.normalizedHeight()),
                    requireResult(data.getResult(), "Extended Crafting table recipe result cannot be empty"),
                    tier
            );
        }
        if (ExtendedCraftingRecipeEditorTypes.isShapelessTableType(type)) {
            var ingredients = compileIngredients(data.getShapelessIngredients(), TABLE_MAX_INPUTS);
            if (ingredients.isEmpty()) {
                throw new IllegalArgumentException("Extended Crafting shapeless table recipe must have at least one ingredient");
            }
            return new ShapelessTableRecipe(ingredients, requireResult(data.getResult(), "Extended Crafting table recipe result cannot be empty"), tier);
        }
        throw new IllegalArgumentException("Unsupported Extended Crafting table recipe type: " + type);
    }

    private static int normalizedTableTier(ResourceLocation type, ExtendedCraftingTableRecipeData data) {
        var tier = data.normalizedTier();
        return tier == 0 ? ExtendedCraftingRecipeEditorTypes.tableTierForType(type) : tier;
    }

    public static Recipe<?> compileEnderCrafter(ResourceLocation type, ExtendedCraftingEnderCrafterRecipeData data) {
        var result = requireResult(data.getResult(), "Extended Crafting ender crafter recipe result cannot be empty");
        var time = Math.max(0, data.getCraftingTime());
        if (ExtendedCraftingRecipeEditorTypes.isShapedEnderType(type)) {
            return new ShapedEnderCrafterRecipe(compilePattern(data.getPattern(), data.getKey(), 3, 3), result, time);
        }
        var ingredients = compileIngredients(data.getShapelessIngredients(), GRID_3X3_INPUTS);
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Extended Crafting shapeless ender recipe must have at least one ingredient");
        }
        return new ShapelessEnderCrafterRecipe(ingredients, result, time);
    }

    public static Recipe<?> compileFluxCrafter(ResourceLocation type, ExtendedCraftingFluxCrafterRecipeData data) {
        var result = requireResult(data.getResult(), "Extended Crafting flux crafter recipe result cannot be empty");
        var powerRequired = Math.max(0, data.getPowerRequired());
        var powerRate = Math.max(0, data.getPowerRate());
        if (ExtendedCraftingRecipeEditorTypes.isShapedFluxType(type)) {
            return new ShapedFluxCrafterRecipe(compilePattern(data.getPattern(), data.getKey(), 3, 3), result, powerRequired, powerRate);
        }
        var ingredients = compileIngredients(data.getShapelessIngredients(), GRID_3X3_INPUTS);
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Extended Crafting shapeless flux recipe must have at least one ingredient");
        }
        return new ShapelessFluxCrafterRecipe(ingredients, result, powerRequired, powerRate);
    }

    public static Recipe<?> compileCombination(ExtendedCraftingCombinationRecipeData data) {
        var input = compileIngredient(data.getInput());
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Extended Crafting combination recipe must have a center input");
        }
        var pedestalItems = compileIngredients(data.getPedestalItems(), COMBINATION_MAX_PEDESTALS);
        if (pedestalItems.isEmpty()) {
            throw new IllegalArgumentException("Extended Crafting combination recipe must have at least one pedestal item");
        }
        return new CombinationRecipe(
                input,
                pedestalItems,
                requireResult(data.getResult(), "Extended Crafting combination recipe result cannot be empty"),
                Math.max(0, data.getPowerCost()),
                Math.max(0, data.getPowerRate())
        );
    }

    public static Recipe<?> compileCompressor(ExtendedCraftingCompressorRecipeData data) {
        var inputs = NonNullList.<IngredientWithCount>create();
        for (var input : safeList(data.getInputs())) {
            if (inputs.size() >= COMPRESSOR_MAX_INPUTS) {
                break;
            }
            var compiled = compileCountedIngredient(input);
            if (compiled != null) {
                inputs.add(compiled);
            }
        }
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("Extended Crafting compressor recipe must have at least one counted input");
        }
        var catalyst = compileIngredient(data.getCatalyst());
        if (catalyst.isEmpty()) {
            throw new IllegalArgumentException("Extended Crafting compressor recipe must have a catalyst");
        }
        return new CompressorRecipe(
                inputs,
                requireResult(data.getResult(), "Extended Crafting compressor recipe result cannot be empty"),
                catalyst,
                Math.max(0, data.getPowerCost()),
                Math.max(0, data.getPowerRate())
        );
    }

    public static Recipe<?> compileUltimateSingularity(ExtendedCraftingUltimateSingularityRecipeData data) {
        return new UltimateSingularityRecipe(requireResult(data.getResult(), "Extended Crafting ultimate singularity result cannot be empty"));
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
        var safeWidth = Math.clamp(width, 1, 9);
        var safeHeight = Math.clamp(height, 1, 9);
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

    private static IngredientWithCount compileCountedIngredient(ExtendedCraftingCountedIngredientData input) {
        if (input == null) {
            return null;
        }
        var values = compileIngredientValues(input.getIngredient());
        if (values.length == 0) {
            return null;
        }
        return new IngredientWithCount(values, Math.max(1, input.getCount()));
    }

    private static Ingredient.Value[] compileIngredientValues(RecipeIngredient ingredient) {
        if (ingredient == null || ingredient.getValues() == null || ingredient.getValues().isEmpty()) {
            return new Ingredient.Value[0];
        }
        var values = new ArrayList<Ingredient.Value>();
        for (var value : ingredient.getValues()) {
            var compiled = compileIngredientValue(value);
            if (compiled != null) {
                values.add(compiled);
            }
        }
        return values.toArray(Ingredient.Value[]::new);
    }

    private static Ingredient.Value compileIngredientValue(RecipeIngredientValue value) {
        if (value == null || value.getKind() == null) {
            return null;
        }
        if (value.getKind() == IngredientValueKind.ITEM) {
            var stack = value.getItem() == null ? ItemStack.EMPTY : value.getItem().copyWithCount(1);
            return stack.isEmpty() || stack.is(Items.AIR) ? null : new Ingredient.ItemValue(stack);
        }
        if (value.getKind() == IngredientValueKind.TAG) {
            return value.getTag() == null
                    ? null
                    : new Ingredient.TagValue(TagKey.create(Registries.ITEM, value.getTag()));
        }
        throw new IllegalArgumentException("Extended Crafting counted ingredients do not support item abilities");
    }

    private static Ingredient compileIngredient(RecipeIngredient ingredient) {
        return ingredient == null ? Ingredient.EMPTY : ingredient.compile();
    }

    private static ItemStack requireResult(ItemStack stack, String message) {
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            throw new IllegalArgumentException(message);
        }
        var copy = stack.copy();
        copy.setCount(Math.clamp(copy.getCount(), 1, 99));
        return copy;
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list.stream().filter(Objects::nonNull).toList();
    }
}
