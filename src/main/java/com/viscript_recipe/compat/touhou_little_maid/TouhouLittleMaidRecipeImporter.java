package com.viscript_recipe.compat.touhou_little_maid;

import com.github.tartaricacid.touhoulittlemaid.crafting.AltarRecipe;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.touhou_little_maid.TouhouLittleMaidAltarRecipeData;
import com.viscript_recipe.data.touhou_little_maid.TouhouLittleMaidRecipeEditorTypes;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;

/**
 * Imports Touhou Little Maid altar recipes without losing their power or entity output fields.
 */
public final class TouhouLittleMaidRecipeImporter implements RecipeImportHandler {
    public static final TouhouLittleMaidRecipeImporter INSTANCE = new TouhouLittleMaidRecipeImporter();

    private TouhouLittleMaidRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && holder.value() instanceof AltarRecipe;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (!(holder.value() instanceof AltarRecipe recipe)) {
            return null;
        }
        var ingredients = new ArrayList<RecipeIngredient>();
        for (var ingredient : recipe.getIngredients()) {
            if (ingredient != null && !ingredient.isEmpty()) {
                ingredients.add(RecipeImporter.importIngredient(ingredient));
            }
        }
        if (ingredients.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
        }
        if (ingredients.size() > TouhouLittleMaidAltarRecipeData.INPUT_COUNT) {
            throw new RecipeImportException(
                    "viscript_recipe.editor.import_recipe.error.too_many_ingredients",
                    ingredients.size(),
                    TouhouLittleMaidAltarRecipeData.INPUT_COUNT
            );
        }
        var data = new TouhouLittleMaidAltarRecipeData()
                .setIngredients(ingredients)
                .setResult(RecipeImporter.copyResult(recipe, provider))
                .setPower(recipe.getPower())
                .setEntityType(recipe.getEntityType())
                .setLangKey(recipe.getLangKey());
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), TouhouLittleMaidRecipeEditorTypes.ALTAR_RECIPE)
                .setTouhouLittleMaidAltar(data));
    }
}
