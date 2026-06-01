package com.viscript_recipe.data.iceandfire;

import com.viscript_recipe.data.RecipeDataAccessors;

public final class IceAndFireRecipeDataAccessors {
    private IceAndFireRecipeDataAccessors() {
    }

    public static void register() {
        RecipeDataAccessors.registerType(DragonForgeRecipeData.class, DragonForgeRecipeData::new);
    }
}
