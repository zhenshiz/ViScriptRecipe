package com.viscript_recipe.compat.extendedcrafting;

import com.blakebr0.cucumber.crafting.ingredient.IngredientWithCount;
import com.blakebr0.extendedcrafting.crafting.recipe.CombinationRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.CompressorRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapedEnderCrafterRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapedFluxCrafterRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapedTableRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapelessEnderCrafterRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapelessFluxCrafterRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.ShapelessTableRecipe;
import com.blakebr0.extendedcrafting.crafting.recipe.UltimateSingularityRecipe;
import com.viscript_recipe.data.extendedcrafting.ExtendedCraftingCombinationRecipeData;
import com.viscript_recipe.data.extendedcrafting.ExtendedCraftingCompressorRecipeData;
import com.viscript_recipe.data.extendedcrafting.ExtendedCraftingCountedIngredientData;
import com.viscript_recipe.data.extendedcrafting.ExtendedCraftingEnderCrafterRecipeData;
import com.viscript_recipe.data.extendedcrafting.ExtendedCraftingFluxCrafterRecipeData;
import com.viscript_recipe.data.extendedcrafting.ExtendedCraftingRecipeEditorTypes;
import com.viscript_recipe.data.extendedcrafting.ExtendedCraftingTableRecipeData;
import com.viscript_recipe.data.extendedcrafting.ExtendedCraftingUltimateSingularityRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;

public final class ExtendedCraftingRecipeImporter implements RecipeImportHandler {
    public static final ExtendedCraftingRecipeImporter INSTANCE = new ExtendedCraftingRecipeImporter();

    private ExtendedCraftingRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        if (holder == null || holder.value() == null) {
            return false;
        }
        var recipe = holder.value();
        return recipe instanceof CombinationRecipe
                || recipe instanceof CompressorRecipe
                || recipe instanceof UltimateSingularityRecipe
                || recipe instanceof ShapedTableRecipe
                || recipe instanceof ShapelessTableRecipe
                || recipe instanceof ShapedEnderCrafterRecipe
                || recipe instanceof ShapelessEnderCrafterRecipe
                || recipe instanceof ShapedFluxCrafterRecipe
                || recipe instanceof ShapelessFluxCrafterRecipe;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        var recipe = holder.value();
        if (recipe instanceof CombinationRecipe combination) {
            var data = new ExtendedCraftingCombinationRecipeData()
                    .setInput(RecipeImporter.importIngredient(combination.getInput()))
                    .setPedestalItems(new ArrayList<>(RecipeImporter.importIngredientList(combination.getIngredients(), 8)))
                    .setResult(RecipeImporter.copyResult(combination, provider))
                    .setPowerCost(Math.max(0, combination.getPowerCost()))
                    .setPowerRate(Math.max(0, combination.getPowerRate()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), ExtendedCraftingRecipeEditorTypes.COMBINATION)
                    .setExtendedCraftingCombination(data));
        }
        if (recipe instanceof CompressorRecipe compressor) {
            var inputs = new ArrayList<ExtendedCraftingCountedIngredientData>();
            var ingredients = compressor.getIngredients();
            for (int i = 0; i < ingredients.size(); i++) {
                var ingredient = ingredients.get(i);
                if (ingredient == null || ingredient.isEmpty()) {
                    continue;
                }
                inputs.add(new ExtendedCraftingCountedIngredientData()
                        .setIngredient(importCountedIngredient(ingredient))
                        .setCount(Math.max(1, compressor.getCount(i))));
            }
            var data = new ExtendedCraftingCompressorRecipeData()
                    .setInputs(inputs)
                    .setCatalyst(RecipeImporter.importIngredient(compressor.getCatalyst()))
                    .setResult(RecipeImporter.copyResult(compressor, provider))
                    .setPowerCost(Math.max(0, compressor.getPowerCost()))
                    .setPowerRate(Math.max(0, compressor.getPowerRate()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), ExtendedCraftingRecipeEditorTypes.COMPRESSOR_RECIPE)
                    .setExtendedCraftingCompressor(data));
        }
        if (recipe instanceof UltimateSingularityRecipe ultimateSingularity) {
            var data = new ExtendedCraftingUltimateSingularityRecipeData()
                    .setResult(RecipeImporter.copyResult(ultimateSingularity, provider));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), ExtendedCraftingRecipeEditorTypes.ULTIMATE_SINGULARITY)
                    .setExtendedCraftingUltimateSingularity(data));
        }
        if (recipe instanceof ShapedTableRecipe shapedTable) {
            var pattern = RecipeImporter.importShapedPattern(shapedTable.getIngredients(), shapedTable.getWidth(), shapedTable.getHeight());
            var data = new ExtendedCraftingTableRecipeData()
                    .setWidth(shapedTable.getWidth())
                    .setHeight(shapedTable.getHeight())
                    .setTier(Math.max(1, shapedTable.getTier()))
                    .setPattern(pattern.pattern())
                    .setKey(pattern.key())
                    .setResult(RecipeImporter.copyResult(shapedTable, provider));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), ExtendedCraftingRecipeEditorTypes.SHAPED_TABLE)
                    .setExtendedCraftingTable(data));
        }
        if (recipe instanceof ShapelessTableRecipe shapelessTable) {
            var tier = Math.max(1, shapelessTable.getTier());
            var gridSize = ExtendedCraftingRecipeEditorTypes.tableGridSizeForTier(tier);
            var data = new ExtendedCraftingTableRecipeData()
                    .setTier(tier)
                    .setWidth(gridSize)
                    .setHeight(gridSize)
                    .setShapelessIngredients(new ArrayList<>(RecipeImporter.importIngredientList(shapelessTable.getIngredients(), 81)))
                    .setResult(RecipeImporter.copyResult(shapelessTable, provider));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), ExtendedCraftingRecipeEditorTypes.SHAPELESS_TABLE)
                    .setExtendedCraftingTable(data));
        }
        if (recipe instanceof ShapedEnderCrafterRecipe shapedEnder) {
            var pattern = RecipeImporter.importShapedPattern(shapedEnder.getIngredients(), shapedEnder.getWidth(), shapedEnder.getHeight());
            var data = new ExtendedCraftingEnderCrafterRecipeData()
                    .setPattern(pattern.pattern())
                    .setKey(pattern.key())
                    .setResult(RecipeImporter.copyResult(shapedEnder, provider))
                    .setCraftingTime(Math.max(0, shapedEnder.getCraftingTime()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), ExtendedCraftingRecipeEditorTypes.SHAPED_ENDER_CRAFTER)
                    .setExtendedCraftingEnderCrafter(data));
        }
        if (recipe instanceof ShapelessEnderCrafterRecipe shapelessEnder) {
            var data = new ExtendedCraftingEnderCrafterRecipeData()
                    .setShapelessIngredients(new ArrayList<>(RecipeImporter.importIngredientList(shapelessEnder.getIngredients(), 9)))
                    .setResult(RecipeImporter.copyResult(shapelessEnder, provider))
                    .setCraftingTime(Math.max(0, shapelessEnder.getCraftingTime()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), ExtendedCraftingRecipeEditorTypes.SHAPELESS_ENDER_CRAFTER)
                    .setExtendedCraftingEnderCrafter(data));
        }
        if (recipe instanceof ShapedFluxCrafterRecipe shapedFlux) {
            var pattern = RecipeImporter.importShapedPattern(shapedFlux.getIngredients(), shapedFlux.getWidth(), shapedFlux.getHeight());
            var data = new ExtendedCraftingFluxCrafterRecipeData()
                    .setPattern(pattern.pattern())
                    .setKey(pattern.key())
                    .setResult(RecipeImporter.copyResult(shapedFlux, provider))
                    .setPowerRequired(Math.max(0, shapedFlux.getPowerRequired()))
                    .setPowerRate(Math.max(0, shapedFlux.getPowerRate()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), ExtendedCraftingRecipeEditorTypes.SHAPED_FLUX_CRAFTER)
                    .setExtendedCraftingFluxCrafter(data));
        }
        if (recipe instanceof ShapelessFluxCrafterRecipe shapelessFlux) {
            var data = new ExtendedCraftingFluxCrafterRecipeData()
                    .setShapelessIngredients(new ArrayList<>(RecipeImporter.importIngredientList(shapelessFlux.getIngredients(), 9)))
                    .setResult(RecipeImporter.copyResult(shapelessFlux, provider))
                    .setPowerRequired(Math.max(0, shapelessFlux.getPowerRequired()))
                    .setPowerRate(Math.max(0, shapelessFlux.getPowerRate()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), ExtendedCraftingRecipeEditorTypes.SHAPELESS_FLUX_CRAFTER)
                    .setExtendedCraftingFluxCrafter(data));
        }
        return null;
    }

    private static com.viscript_recipe.data.RecipeIngredient importCountedIngredient(Ingredient ingredient) throws RecipeImportException {
        try {
            return RecipeImporter.importIngredient(ingredient);
        } catch (RecipeImportException exception) {
            if (ingredient != null && ingredient.isCustom() && ingredient.getCustomIngredient() instanceof IngredientWithCount counted) {
                return RecipeImporter.importItemStacks(counted.getItems().toList());
            }
            throw exception;
        }
    }
}
