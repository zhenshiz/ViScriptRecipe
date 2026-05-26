package com.viscript_recipe.data.vanilla;

import com.viscript_recipe.data.RecipeDataAccessors;

public final class VanillaRecipeDataAccessors {
    private VanillaRecipeDataAccessors() {
    }

    public static void register() {
        RecipeDataAccessors.registerType(CraftingRemainderRule.class, CraftingRemainderRule::new);
        RecipeDataAccessors.registerType(ShapedKeyEntry.class, ShapedKeyEntry::new);
        RecipeDataAccessors.registerType(ShapedCraftingRecipeData.class, ShapedCraftingRecipeData::new);
        RecipeDataAccessors.registerType(ShapelessCraftingRecipeData.class, ShapelessCraftingRecipeData::new);
        RecipeDataAccessors.registerType(CookingRecipeData.class, CookingRecipeData::new);
        RecipeDataAccessors.registerType(StonecuttingRecipeData.class, StonecuttingRecipeData::new);
        RecipeDataAccessors.registerType(SmithingTransformRecipeData.class, SmithingTransformRecipeData::new);
    }
}
