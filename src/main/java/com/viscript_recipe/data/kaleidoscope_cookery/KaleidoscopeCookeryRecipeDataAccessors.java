package com.viscript_recipe.data.kaleidoscope_cookery;

import com.viscript_recipe.data.RecipeDataAccessors;

public final class KaleidoscopeCookeryRecipeDataAccessors {
    private KaleidoscopeCookeryRecipeDataAccessors() {
    }

    public static void register() {
        RecipeDataAccessors.registerType(KaleidoscopePotRecipeData.class, KaleidoscopePotRecipeData::new);
        RecipeDataAccessors.registerType(KaleidoscopeStockpotRecipeData.class, KaleidoscopeStockpotRecipeData::new);
        RecipeDataAccessors.registerType(KaleidoscopeMillstoneRecipeData.class, KaleidoscopeMillstoneRecipeData::new);
        RecipeDataAccessors.registerType(KaleidoscopeChoppingBoardRecipeData.class, KaleidoscopeChoppingBoardRecipeData::new);
        RecipeDataAccessors.registerType(KaleidoscopeSteamerRecipeData.class, KaleidoscopeSteamerRecipeData::new);
        RecipeDataAccessors.registerType(KaleidoscopeTeapotRecipeData.class, KaleidoscopeTeapotRecipeData::new);
    }
}
