package com.viscript_recipe.compat.neoecoae;

import com.viscript_recipe.compat.neoecoae.data.NeoEcoRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;

public final class NeoEcoRecipeImporter implements RecipeImportHandler {
    public static final NeoEcoRecipeImporter INSTANCE = new NeoEcoRecipeImporter();
    private NeoEcoRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && NeoEcoRecipeFactory.supports(holder.value());
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) {
        if (!canImport(holder)) return null;
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), NeoEcoRecipeFactory.type(holder.value()))
                .setData(new NeoEcoRecipeData().setRecipeJson(NeoEcoRecipeFactory.encode(holder.value(), provider))));
    }
}
