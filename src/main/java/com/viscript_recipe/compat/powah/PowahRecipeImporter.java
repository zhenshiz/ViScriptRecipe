package com.viscript_recipe.compat.powah;

import com.viscript_recipe.compat.powah.data.PowahRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;
import owmii.powah.block.energizing.EnergizingRecipe;

public final class PowahRecipeImporter implements RecipeImportHandler {
    public static final PowahRecipeImporter INSTANCE = new PowahRecipeImporter();
    private PowahRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) { return holder != null && holder.value() instanceof EnergizingRecipe; }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) {
        if (!canImport(holder)) return null;
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), PowahRecipeEditorTypes.ENERGIZING)
                .setData(new PowahRecipeData().setRecipeJson(PowahRecipeFactory.encode(holder.value(), provider))));
    }
}
