package com.viscript_recipe.compat.iceandfire;

import com.iafenvoy.iceandfire.recipe.DragonForgeRecipe;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.iceandfire.DragonForgeRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class IceAndFireRecipeImporter implements RecipeImportHandler {
    public static final IceAndFireRecipeImporter INSTANCE = new IceAndFireRecipeImporter();

    private IceAndFireRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && holder.value() instanceof DragonForgeRecipe;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (holder.value() instanceof DragonForgeRecipe recipe) {
            var data = new DragonForgeRecipeData()
                    .setInput(RecipeImporter.importIngredient(recipe.getInput()))
                    .setBlood(RecipeImporter.importIngredient(recipe.getBlood()))
                    .setResult(RecipeImporter.copyResult(recipe, provider))
                    .setDragonType(IceAndFireRecipeFactory.normalizeDragonType(recipe.getDragonType()))
                    .setCookTime(Math.max(1, recipe.getCookTime()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.ICEANDFIRE_DRAGONFORGE)
                    .setIceAndFireDragonForge(data));
        }
        return null;
    }
}
