package com.viscript_recipe.data.ars_nouveau;

import com.viscript_recipe.data.RecipeDataAccessors;

public final class ArsNouveauRecipeDataAccessors {
    private ArsNouveauRecipeDataAccessors() {
    }

    public static void register() {
        RecipeDataAccessors.registerType(ArsNouveauApparatusRecipeData.class, ArsNouveauApparatusRecipeData::new);
        RecipeDataAccessors.registerType(ArsNouveauArmorUpgradeRecipeData.class, ArsNouveauArmorUpgradeRecipeData::new);
        RecipeDataAccessors.registerType(ArsNouveauCrushOutputData.class, ArsNouveauCrushOutputData::new);
        RecipeDataAccessors.registerType(ArsNouveauCrushRecipeData.class, ArsNouveauCrushRecipeData::new);
        RecipeDataAccessors.registerType(ArsNouveauEnchantmentRecipeData.class, ArsNouveauEnchantmentRecipeData::new);
        RecipeDataAccessors.registerType(ArsNouveauGlyphRecipeData.class, ArsNouveauGlyphRecipeData::new);
        RecipeDataAccessors.registerType(ArsNouveauImbuementRecipeData.class, ArsNouveauImbuementRecipeData::new);
        RecipeDataAccessors.registerType(ArsNouveauPedestalOnlyRecipeData.class, ArsNouveauPedestalOnlyRecipeData::new);
    }
}
