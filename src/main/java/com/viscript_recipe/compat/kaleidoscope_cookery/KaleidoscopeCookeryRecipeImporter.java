package com.viscript_recipe.compat.kaleidoscope_cookery;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.*;
import com.viscript_recipe.compat.kaleidoscope_cookery.data.*;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;

public final class KaleidoscopeCookeryRecipeImporter implements RecipeImportHandler {
    public static final KaleidoscopeCookeryRecipeImporter INSTANCE = new KaleidoscopeCookeryRecipeImporter();

    private KaleidoscopeCookeryRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        if (holder == null) {
            return false;
        }
        var recipe = holder.value();
        return recipe instanceof PotRecipe
                || recipe instanceof StockpotRecipe
                || recipe instanceof MillstoneRecipe
                || recipe instanceof ChoppingBoardRecipe
                || recipe instanceof SteamerRecipe
                || recipe instanceof TeapotRecipe;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        var recipe = holder.value();
        if (recipe instanceof PotRecipe pot) {
            var data = new KaleidoscopePotRecipeData()
                    .setIngredients(new ArrayList<>(RecipeImporter.importIngredientList(pot.ingredients(), 9)))
                    .setResult(RecipeImporter.copyResult(pot, provider))
                    .setCarrier(importOptional(pot.carrier()))
                    .setTime(Math.max(1, pot.time()))
                    .setStirFryCount(Math.max(0, pot.stirFryCount()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.KALEIDOSCOPE_COOKERY_POT).setData(data));
        }
        if (recipe instanceof StockpotRecipe stockpot) {
            var data = new KaleidoscopeStockpotRecipeData()
                    .setIngredients(new ArrayList<>(RecipeImporter.importIngredientList(stockpot.ingredients(), 9)))
                    .setSoupBase(nonNullId(stockpot.soupBase(), ResourceLocation.withDefaultNamespace("water")))
                    .setResult(RecipeImporter.copyResult(stockpot, provider))
                    .setTime(Math.max(1, stockpot.time()))
                    .setCarrier(importOptional(stockpot.carrier()))
                    .setCookingTexture(stockpot.cookingTexture())
                    .setFinishedTexture(stockpot.finishedTexture())
                    .setCookingBubbleColor(stockpot.cookingBubbleColor())
                    .setFinishedBubbleColor(stockpot.finishedBubbleColor());
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.KALEIDOSCOPE_COOKERY_STOCKPOT).setData(data));
        }
        if (recipe instanceof MillstoneRecipe millstone) {
            var data = new KaleidoscopeMillstoneRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(millstone.ingredient()))
                    .setResults(millstone.results().stream()
                            .limit(KaleidoscopeMillstoneRecipeData.MAX_RESULTS)
                            .map(output -> RecipeOutputData.of(output.stack(), output.chance()))
                            .toList());
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.KALEIDOSCOPE_COOKERY_MILLSTONE).setData(data));
        }
        if (recipe instanceof ChoppingBoardRecipe choppingBoard) {
            var data = new KaleidoscopeChoppingBoardRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(choppingBoard.getIngredient()))
                    .setResult(RecipeImporter.copyResult(choppingBoard, provider))
                    .setCutCount(Math.max(1, choppingBoard.getCutCount()))
                    .setModelId(choppingBoard.getModelId());
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.KALEIDOSCOPE_COOKERY_CHOPPING_BOARD).setData(data));
        }
        if (recipe instanceof SteamerRecipe steamer) {
            var data = new KaleidoscopeSteamerRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(steamer.getIngredient()))
                    .setResult(RecipeImporter.copyResult(steamer, provider))
                    .setCookTick(Math.max(1, steamer.getCookTick()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.KALEIDOSCOPE_COOKERY_STEAMER).setData(data));
        }
        if (recipe instanceof TeapotRecipe teapot) {
            var data = new KaleidoscopeTeapotRecipeData()
                    .setTeaFluid(nonNullId(teapot.teaFluid(), ResourceLocation.withDefaultNamespace("water")))
                    .setIngredient(RecipeImporter.importIngredient(teapot.ingredient())
                            .setCount(Math.max(1, teapot.ingredientCount())))
                    .setTime(Math.max(1, teapot.time()))
                    .setResult(RecipeImporter.copyResult(teapot, provider));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.KALEIDOSCOPE_COOKERY_TEAPOT).setData(data));
        }
        return null;
    }

    private static RecipeIngredient importOptional(Ingredient ingredient) throws RecipeImportException {
        return ingredient == null || ingredient.isEmpty() ? RecipeIngredient.empty() : RecipeImporter.importIngredient(ingredient);
    }

    private static ResourceLocation nonNullId(ResourceLocation id, ResourceLocation fallback) {
        return id == null ? fallback : id;
    }
}
