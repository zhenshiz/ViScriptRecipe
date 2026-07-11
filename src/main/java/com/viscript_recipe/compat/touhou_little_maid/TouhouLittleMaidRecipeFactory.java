package com.viscript_recipe.compat.touhou_little_maid;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import com.viscript_recipe.data.touhou_little_maid.TouhouLittleMaidAltarRecipeData;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Creates Touhou Little Maid's native altar recipes from editor data.
 */
public final class TouhouLittleMaidRecipeFactory {
    private TouhouLittleMaidRecipeFactory() {
    }

    public static Recipe<?> compileAltar(TouhouLittleMaidAltarRecipeData data) {
        var ingredients = NonNullList.<Ingredient>create();
        for (var ingredientData : data.normalizedIngredients()) {
            var ingredient = ingredientData == null ? Ingredient.EMPTY : ingredientData.compile();
            if (!ingredient.isEmpty()) {
                ingredients.add(ingredient);
            }
        }
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Touhou Little Maid altar recipe must contain at least one ingredient");
        }
        if (ingredients.size() > TouhouLittleMaidAltarRecipeData.INPUT_COUNT) {
            throw new IllegalArgumentException("Touhou Little Maid altar recipe accepts at most six ingredients");
        }
        var result = requireResult(data.getResult());
        var entityType = data.getEntityType();
        if (entityType == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(entityType)) {
            throw new IllegalArgumentException("Unknown altar output entity type: " + entityType);
        }
        var power = data.getPower();
        if (!Float.isFinite(power) || power < 0) {
            throw new IllegalArgumentException("Altar power cost must be a finite non-negative number");
        }
        return new AltarRecipe(
                "",
                CraftingBookCategory.MISC,
                ingredients,
                power,
                result,
                entityType,
                data.getLangKey() == null ? "" : data.getLangKey()
        );
    }

    private static ItemStack requireResult(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException("Touhou Little Maid altar result cannot be empty");
        }
        return stack.copy();
    }
}
