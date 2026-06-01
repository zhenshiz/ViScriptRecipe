package com.viscript_recipe.data.irons_spellbooks;

import com.viscript_recipe.data.RecipeDataAccessors;

public final class IronSpellbooksRecipeDataAccessors {
    private IronSpellbooksRecipeDataAccessors() {
    }

    public static void register() {
        RecipeDataAccessors.registerType(IronArcaneAnvilRecipeData.class, IronArcaneAnvilRecipeData::new);
        RecipeDataAccessors.registerType(IronAlchemistCauldronRecipeData.class, IronAlchemistCauldronRecipeData::new);
        RecipeDataAccessors.registerType(IronNoAdditionSmithingRecipeData.class, IronNoAdditionSmithingRecipeData::new);
    }
}
