package com.viscript_recipe.compat.avaritia;

import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.avaritia.*;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
import committee.nova.mods.avaritia.common.crafting.recipe.*;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public final class AvaritiaRecipeFactory {
    private static final int MAX_TABLE_INPUTS = 81;

    private AvaritiaRecipeFactory() {
    }

    public static Recipe<?> compileTable(ResourceLocation type, AvaritiaTableRecipeData data) {
        var tier = data.normalizedTier();
        if (AvaritiaRecipeEditorTypes.isShapedTableType(type)) {
            return new ShapedTableCraftingRecipe(
                    compilePattern(data.getPattern(), data.getKey(), data.normalizedWidth(), data.normalizedHeight()),
                    requireResult(data.getResult(), "Avaritia table recipe result cannot be empty"),
                    tier,
                    data.isCompatible()
            );
        }
        if (AvaritiaRecipeEditorTypes.isNoConsumeCatalystType(type)) {
            return new NoConsumeCatalystShapedRecipe(
                    compilePattern(data.getPattern(), data.getKey(), data.normalizedWidth(), data.normalizedHeight()),
                    requireResult(data.getResult(), "Avaritia no-consume catalyst recipe result cannot be empty"),
                    tier == 0 ? 4 : tier
            );
        }
        if (AvaritiaRecipeEditorTypes.isShapelessTableType(type)) {
            var ingredients = compileIngredients(data.getShapelessIngredients(), MAX_TABLE_INPUTS, false);
            return new ShapelessTableCraftingRecipe(
                    ingredients,
                    requireResult(data.getResult(), "Avaritia shapeless table recipe result cannot be empty"),
                    tier
            );
        }
        throw new IllegalArgumentException("Unsupported Avaritia table recipe type: " + type);
    }

    public static Recipe<?> compileCompressor(AvaritiaCompressorRecipeData data) {
        var ingredient = compileIngredient(data.getIngredient());
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException("Avaritia compressor recipe ingredient cannot be empty");
        }
        return new CompressorRecipe(
                ingredient,
                requireResult(data.getResult(), "Avaritia compressor recipe result cannot be empty"),
                Math.max(1, data.getInputCount()),
                Math.max(1, data.getTimeCost())
        );
    }

    public static Recipe<?> compileExtremeSmithing(AvaritiaExtremeSmithingRecipeData data) {
        return new ExtremeSmithingRecipe(
                requireIngredient(data.getTemplate(), "Avaritia extreme smithing template cannot be empty"),
                requireIngredient(data.getBase(), "Avaritia extreme smithing base cannot be empty"),
                compileExtremeSmithingAdditions(data.normalizedAdditions()),
                requireResult(data.getResult(), "Avaritia extreme smithing result cannot be empty")
        );
    }

    public static Recipe<?> compileInfinityCatalyst(AvaritiaInfinityCatalystRecipeData data) {
        return new InfinityCatalystCraftRecipe(
                normalizedGroup(data.getGroup()),
                compileIngredients(data.getIngredients(), MAX_TABLE_INPUTS, false),
                Math.max(1, data.getCount())
        );
    }

    public static Recipe<?> compileEternalSingularity(AvaritiaEternalSingularityRecipeData data) {
        return new EternalSingularityCraftRecipe(
                compileIngredients(data.getIngredients(), MAX_TABLE_INPUTS, true),
                Math.max(1, data.getCount())
        );
    }

    public static Recipe<?> compileFullMatterCluster(AvaritiaFullMatterClusterRecipeData data) {
        return new FullMatterClusterRecipe(
                normalizedGroup(data.getGroup()),
                compileIngredients(data.getIngredients(), MAX_TABLE_INPUTS, false),
                Math.max(1, data.getCount())
        );
    }

    public static ItemStack defaultItemStack(String itemId, Item fallback) {
        var id = ResourceLocation.tryParse(itemId);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            return new ItemStack(fallback);
        }
        var item = BuiltInRegistries.ITEM.get(id);
        return new ItemStack(item == Items.AIR ? fallback : item);
    }

    private static ShapedRecipePattern compilePattern(List<String> pattern, List<ShapedKeyEntry> key, int width, int height) {
        var normalizedPattern = normalizePattern(pattern, width, height);
        if (normalizedPattern.stream().allMatch(String::isBlank)) {
            throw new IllegalArgumentException("Avaritia shaped table recipe pattern cannot be empty");
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

    private static NonNullList<Ingredient> compileIngredients(List<RecipeIngredient> ingredients, int maxCount, boolean allowEmpty) {
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
        if (compiled.isEmpty() && !allowEmpty) {
            throw new IllegalArgumentException("Avaritia shapeless table recipe must have at least one ingredient");
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

    private static Ingredient compileExtremeSmithingAdditions(List<RecipeIngredient> additions) {
        var compiled = safeList(additions).stream()
                .map(AvaritiaRecipeFactory::compileIngredient)
                .filter(ingredient -> !ingredient.isEmpty())
                .toArray(Ingredient[]::new);
        if (compiled.length < 3) {
            throw new IllegalArgumentException("Avaritia extreme smithing requires three addition ingredients");
        }
        return net.neoforged.neoforge.common.crafting.CompoundIngredient.of(compiled);
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

    private static String normalizedGroup(String group) {
        return group == null || group.isBlank() ? "default" : group;
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list.stream().filter(Objects::nonNull).toList();
    }
}
