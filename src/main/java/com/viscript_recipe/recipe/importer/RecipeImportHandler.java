package com.viscript_recipe.recipe.importer;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public interface RecipeImportHandler {
    boolean canImport(RecipeHolder<?> holder);

    @Nullable
    RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException;
}
