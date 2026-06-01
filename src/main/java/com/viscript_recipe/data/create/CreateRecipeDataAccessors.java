package com.viscript_recipe.data.create;

import com.viscript_recipe.data.RecipeDataAccessors;

public final class CreateRecipeDataAccessors {
    private CreateRecipeDataAccessors() {
    }

    public static void register() {
        RecipeDataAccessors.registerType(CreateFluidIngredientData.class, CreateFluidIngredientData::new);
        RecipeDataAccessors.registerType(CreateMechanicalCraftingRecipeData.class, CreateMechanicalCraftingRecipeData::new);
        RecipeDataAccessors.registerType(CreateProcessingOutputData.class, CreateProcessingOutputData::new);
        RecipeDataAccessors.registerType(CreateProcessingRecipeData.class, CreateProcessingRecipeData::new);
        RecipeDataAccessors.registerType(CreateSequencedAssemblyRecipeData.class, CreateSequencedAssemblyRecipeData::new);
        RecipeDataAccessors.registerType(CreateSequencedAssemblyStepData.class, CreateSequencedAssemblyStepData::new);
    }
}
