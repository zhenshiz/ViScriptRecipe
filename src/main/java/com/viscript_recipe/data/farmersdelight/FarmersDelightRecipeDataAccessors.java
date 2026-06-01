package com.viscript_recipe.data.farmersdelight;

import com.viscript_recipe.data.RecipeDataAccessors;

public final class FarmersDelightRecipeDataAccessors {
    private FarmersDelightRecipeDataAccessors() {
    }

    public static void register() {
        RecipeDataAccessors.registerType(FarmerCookingPotRecipeData.class, FarmerCookingPotRecipeData::new);
        RecipeDataAccessors.registerType(FarmerCuttingRecipeData.class, FarmerCuttingRecipeData::new);
        RecipeDataAccessors.registerType(FarmerCuttingResultData.class, FarmerCuttingResultData::new);
    }
}
