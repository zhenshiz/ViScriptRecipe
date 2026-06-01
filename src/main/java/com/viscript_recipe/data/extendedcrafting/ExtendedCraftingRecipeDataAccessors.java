package com.viscript_recipe.data.extendedcrafting;

import com.viscript_recipe.data.RecipeDataAccessors;

public final class ExtendedCraftingRecipeDataAccessors {
    private ExtendedCraftingRecipeDataAccessors() {
    }

    public static void register() {
        RecipeDataAccessors.registerType(ExtendedCraftingCombinationRecipeData.class, ExtendedCraftingCombinationRecipeData::new);
        RecipeDataAccessors.registerType(ExtendedCraftingCompressorRecipeData.class, ExtendedCraftingCompressorRecipeData::new);
        RecipeDataAccessors.registerType(ExtendedCraftingCountedIngredientData.class, ExtendedCraftingCountedIngredientData::new);
        RecipeDataAccessors.registerType(ExtendedCraftingEnderCrafterRecipeData.class, ExtendedCraftingEnderCrafterRecipeData::new);
        RecipeDataAccessors.registerType(ExtendedCraftingFluxCrafterRecipeData.class, ExtendedCraftingFluxCrafterRecipeData::new);
        RecipeDataAccessors.registerType(ExtendedCraftingTableRecipeData.class, ExtendedCraftingTableRecipeData::new);
        RecipeDataAccessors.registerType(ExtendedCraftingUltimateSingularityRecipeData.class, ExtendedCraftingUltimateSingularityRecipeData::new);
    }
}
