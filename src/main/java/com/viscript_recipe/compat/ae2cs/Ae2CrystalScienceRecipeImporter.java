package com.viscript_recipe.compat.ae2cs;

import com.viscript_recipe.compat.ae2cs.data.Ae2CrystalScienceRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class Ae2CrystalScienceRecipeImporter implements RecipeImportHandler {
    public static final Ae2CrystalScienceRecipeImporter INSTANCE = new Ae2CrystalScienceRecipeImporter();
    private Ae2CrystalScienceRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && Ae2CrystalScienceRecipeFactory.supports(holder.value());
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) {
        if (!canImport(holder)) return null;
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), Ae2CrystalScienceRecipeFactory.type(holder.value()))
                .setData(new Ae2CrystalScienceRecipeData().setRecipeJson(Ae2CrystalScienceRecipeFactory.encode(holder.value(), provider))));
    }
}
