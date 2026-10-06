package com.viscript_recipe.compat.tacz;

import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.viscript_recipe.compat.tacz.data.TaczIngredientData;
import com.viscript_recipe.compat.tacz.data.TaczRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;

/** 导入枪械工作台配方；JEI 的配件适用查询不属于可合成配方。 */
public final class TaczRecipeImporter implements RecipeImportHandler {
    public static final TaczRecipeImporter INSTANCE = new TaczRecipeImporter();
    private TaczRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && holder.value() instanceof GunSmithTableRecipe;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (!(holder.value() instanceof GunSmithTableRecipe recipe)) return null;
        if (recipe.getInputs().size() > TaczRecipeData.MAX_INPUTS) {
            throw new RecipeImportException("viscript_recipe.editor.tacz.too_many_materials", TaczRecipeData.MAX_INPUTS);
        }
        recipe.init(provider);
        var materials = new ArrayList<TaczIngredientData>();
        for (var input : recipe.getInputs()) materials.add(TaczIngredientData.from(input.getIngredient(), input.getCount(), provider));
        var data = new TaczRecipeData().setMaterials(materials).setResult(recipe.getOutput().copy())
                .setGroup(recipe.getTab()).setInputSlots(Math.max(6, materials.size()));
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), TaczRecipeEditorTypes.CRAFTING).setData(data));
    }
}
