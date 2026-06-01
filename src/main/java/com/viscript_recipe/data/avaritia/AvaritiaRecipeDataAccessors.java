package com.viscript_recipe.data.avaritia;

import com.viscript_recipe.data.RecipeDataAccessors;

public final class AvaritiaRecipeDataAccessors {
    private AvaritiaRecipeDataAccessors() {
    }

    public static void register() {
        RecipeDataAccessors.registerType(AvaritiaCompressorRecipeData.class, AvaritiaCompressorRecipeData::new);
        RecipeDataAccessors.registerType(AvaritiaEternalSingularityRecipeData.class, AvaritiaEternalSingularityRecipeData::new);
        RecipeDataAccessors.registerType(AvaritiaExtremeSmithingRecipeData.class, AvaritiaExtremeSmithingRecipeData::new);
        RecipeDataAccessors.registerType(AvaritiaFullMatterClusterRecipeData.class, AvaritiaFullMatterClusterRecipeData::new);
        RecipeDataAccessors.registerType(AvaritiaInfinityCatalystRecipeData.class, AvaritiaInfinityCatalystRecipeData::new);
        RecipeDataAccessors.registerType(AvaritiaTableRecipeData.class, AvaritiaTableRecipeData::new);
    }
}
