package com.viscript_recipe.compat.alloy_smelter;

import com.viscript_recipe.data.alloy_smelter.AlloySmelterMaterialData;
import com.viscript_recipe.data.alloy_smelter.AlloySmelterRecipeData;
import com.viscript_recipe.data.alloy_smelter.AlloySmelterRecipeEditorTypes;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;
import sk.alloy_smelter.recipe.SmeltingRecipe;

import java.util.ArrayList;

/** Imports Alloy Smelter recipes registered in the vanilla recipe manager. */
public final class AlloySmelterRecipeImporter implements RecipeImportHandler {
    public static final AlloySmelterRecipeImporter INSTANCE = new AlloySmelterRecipeImporter();

    private AlloySmelterRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && holder.value() instanceof SmeltingRecipe;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (!(holder.value() instanceof SmeltingRecipe recipe)) {
            return null;
        }
        var materials = new ArrayList<AlloySmelterMaterialData>();
        for (var material : recipe.getMaterials()) {
            materials.add(new AlloySmelterMaterialData()
                    .setIngredient(RecipeImporter.importIngredient(material.ingredient()))
                    .setCount(Math.max(1, material.count())));
        }
        var data = new AlloySmelterRecipeData()
                .setMaterials(materials)
                .setResult(RecipeImporter.copyStack(recipe.getOutput()))
                .setSmeltingTime(Math.max(0, recipe.getSmeltingTime()))
                .setFuelPerTick(Math.max(0, recipe.fuelPerTick()))
                .setRequiredTier(Math.clamp(recipe.getRequiredTier(), 1, 3));
        var entry = RecipeImporter.baseEntry(holder.id(), AlloySmelterRecipeEditorTypes.SMELTING).setData(data);
        return RecipeImporter.success(entry);
    }
}
