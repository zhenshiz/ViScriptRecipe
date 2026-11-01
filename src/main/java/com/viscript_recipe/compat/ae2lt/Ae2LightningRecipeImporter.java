package com.viscript_recipe.compat.ae2lt;

import com.viscript_recipe.compat.ae2lt.data.Ae2LightningRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class Ae2LightningRecipeImporter implements RecipeImportHandler {
    public static final Ae2LightningRecipeImporter INSTANCE = new Ae2LightningRecipeImporter();
    private Ae2LightningRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && Ae2LightningRecipeFactory.supports(holder.value());
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) {
        if (!canImport(holder)) return null;
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), Ae2LightningRecipeFactory.type(holder.value()))
                .setData(new Ae2LightningRecipeData().setRecipeJson(Ae2LightningRecipeFactory.encode(holder.value(), provider))));
    }
}
