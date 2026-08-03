package com.viscript_recipe.compat.spore;

import com.Harbinger.Spore.Recipes.GraftingRecipe;
import com.Harbinger.Spore.Recipes.SurgeryRecipe;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.spore.SporeGraftingRecipeData;
import com.viscript_recipe.data.spore.SporeRecipeEditorTypes;
import com.viscript_recipe.data.spore.SporeSurgeryRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

public final class SporeRecipeImporter implements RecipeImportHandler {
    public static final SporeRecipeImporter INSTANCE = new SporeRecipeImporter();

    private static final int GRAFTING_FIRST_INPUT_SLOT = 21;

    private SporeRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && (holder.value() instanceof SurgeryRecipe || holder.value() instanceof GraftingRecipe);
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (holder.value() instanceof SurgeryRecipe recipe) {
            var data = new SporeSurgeryRecipeData()
                    .setIngredients(importPositionedIngredients(recipe.getIngredients(), 0, SporeSurgeryRecipeData.INPUT_COUNT))
                    .setResult(RecipeImporter.copyResult(recipe, provider));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), SporeRecipeEditorTypes.SURGERY)
                    .setData(data));
        }
        if (holder.value() instanceof GraftingRecipe recipe) {
            var data = new SporeGraftingRecipeData()
                    .setIngredients(importPositionedIngredients(recipe.getIngredients(), GRAFTING_FIRST_INPUT_SLOT, SporeGraftingRecipeData.INPUT_COUNT))
                    .setResult(RecipeImporter.copyResult(recipe, provider));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), SporeRecipeEditorTypes.GRAFTING)
                    .setData(data));
        }
        return null;
    }

    private static List<RecipeIngredient> importPositionedIngredients(List<Ingredient> source, int offset, int count) throws RecipeImportException {
        var ingredients = new ArrayList<RecipeIngredient>(count);
        for (int i = 0; i < count; i++) {
            var sourceIndex = offset + i;
            var ingredient = sourceIndex < source.size() ? source.get(sourceIndex) : Ingredient.EMPTY;
            ingredients.add(ingredient == null || ingredient.isEmpty()
                    ? RecipeIngredient.empty()
                    : RecipeImporter.importIngredient(ingredient));
        }
        return ingredients;
    }
}
