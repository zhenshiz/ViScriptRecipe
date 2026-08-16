package com.viscript_recipe.compat.alloy_smelter;

import com.viscript_recipe.compat.alloy_smelter.data.AlloySmelterRecipeData;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Items;
import sk.alloy_smelter.recipe.SmeltingRecipe;

/** Converts editor-owned data into Alloy Smelter's native recipe class. */
public final class AlloySmelterRecipeFactory {
    private AlloySmelterRecipeFactory() {
    }

    public static SmeltingRecipe compile(AlloySmelterRecipeData data) {
        var materials = NonNullList.<SmeltingRecipe.Material>create();
        for (var material : data.getMaterials()) {
            var ingredient = material.compile();
            if (!ingredient.isEmpty()) {
                materials.add(SmeltingRecipe.Material.of(ingredient, Math.max(1, material.getCount())));
            }
            if (materials.size() == AlloySmelterRecipeData.MAX_INPUTS) break;
        }
        if (materials.isEmpty()) {
            throw new IllegalArgumentException("Alloy Smelter recipe must contain at least one material");
        }
        var result = data.getResult();
        if (result == null || result.isEmpty() || result.is(Items.AIR)) {
            throw new IllegalArgumentException("Alloy Smelter recipe result cannot be empty");
        }
        return new SmeltingRecipe(
                materials,
                result.copy(),
                Math.max(0, data.getSmeltingTime()),
                Math.max(0, data.getFuelPerTick()),
                Math.clamp(data.getRequiredTier(), 1, 3)
        );
    }
}
