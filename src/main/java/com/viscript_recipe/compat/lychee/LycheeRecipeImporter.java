package com.viscript_recipe.compat.lychee;

import com.viscript_recipe.compat.lychee.data.LycheeRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.util.recipe.ILycheeRecipe;

/** 通过 Lychee 的序列化器导入配方，包括扩展原版合成分类的高级有序合成。 */
public final class LycheeRecipeImporter implements RecipeImportHandler {
    public static final LycheeRecipeImporter INSTANCE = new LycheeRecipeImporter();
    private LycheeRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && holder.value() instanceof ILycheeRecipe<?>
                && LycheeRecipeEditorTypes.MOD_ID.equals(LycheeRecipeFactory.type(holder.value()).getNamespace())
                && LycheeRecipeEditorTypes.TYPES.contains(LycheeRecipeFactory.type(holder.value()).getPath());
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) {
        if (!canImport(holder)) return null;
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), LycheeRecipeFactory.type(holder.value()))
                .setData(new LycheeRecipeData().setRecipeJson(LycheeRecipeFactory.encode(holder.value(), provider))));
    }
}
