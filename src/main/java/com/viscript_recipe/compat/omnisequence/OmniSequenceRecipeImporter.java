package com.viscript_recipe.compat.omnisequence;

import com.viscript_recipe.compat.omnisequence.data.OmniSequenceRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class OmniSequenceRecipeImporter implements RecipeImportHandler {
    public static final OmniSequenceRecipeImporter INSTANCE = new OmniSequenceRecipeImporter();
    private OmniSequenceRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && OmniSequenceRecipeFactory.supports(holder.value());
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) {
        if (!canImport(holder)) return null;
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), OmniSequenceRecipeFactory.type(holder.value()))
                .setData(new OmniSequenceRecipeData().setRecipeJson(OmniSequenceRecipeFactory.encode(holder.value(), provider))));
    }
}
