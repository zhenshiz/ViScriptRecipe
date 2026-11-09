package com.viscript_recipe.compat.kaleidoscope_cookery;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.*;
import com.viscript_recipe.compat.kaleidoscope_cookery.data.*;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;

public final class KaleidoscopeCookeryRecipeImporter implements RecipeImportHandler {
    public static final KaleidoscopeCookeryRecipeImporter INSTANCE = new KaleidoscopeCookeryRecipeImporter();

    private static final ResourceLocation WATER = new ResourceLocation("water");

    private KaleidoscopeCookeryRecipeImporter() {
    }

    @Override
    public boolean canImport(Recipe<?> recipe) {
        if (recipe == null) return false;
        return recipe instanceof PotRecipe
                || recipe instanceof StockpotRecipe
                || recipe instanceof MillstoneRecipe
                || recipe instanceof ChoppingBoardRecipe
                || recipe instanceof SteamerRecipe
                || recipe instanceof TeapotRecipe
                || recipe instanceof BambooTrayRecipe;
    }

    @Override
    public RecipeImportResult tryImport(Recipe<?> recipe, HolderLookup.Provider provider) throws RecipeImportException {
        if (recipe instanceof PotRecipe pot) {
            var data = new KaleidoscopePotRecipeData()
                    .setIngredients(new ArrayList<>(RecipeImporter.importIngredientList(pot.ingredients(), 9)))
                    .setResult(RecipeImporter.copyResult(pot, provider))
                    .setCarrier(importOptional(pot.carrier()))
                    .setTime(Math.max(1, pot.time()))
                    .setStirFryCount(Math.max(0, pot.stirFryCount()));
            return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), KaleidoscopeCookeryRecipeEditorTypes.POT).setData(data));
        }
        if (recipe instanceof StockpotRecipe stockpot) {
            var data = new KaleidoscopeStockpotRecipeData()
                    .setIngredients(new ArrayList<>(RecipeImporter.importIngredientList(stockpot.ingredients(), 9)))
                    .setSoupBase(nonNullId(stockpot.soupBase(), WATER))
                    .setResult(RecipeImporter.copyResult(stockpot, provider))
                    .setTime(Math.max(1, stockpot.time()))
                    .setCarrier(importOptional(stockpot.carrier()))
                    .setCookingTexture(stockpot.cookingTexture())
                    .setFinishedTexture(stockpot.finishedTexture())
                    .setCookingBubbleColor(stockpot.cookingBubbleColor())
                    .setFinishedBubbleColor(stockpot.finishedBubbleColor());
            return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), KaleidoscopeCookeryRecipeEditorTypes.STOCKPOT).setData(data));
        }
        if (recipe instanceof MillstoneRecipe millstone) {
            var data = new KaleidoscopeMillstoneRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(millstone.ingredient()))
                    .setResults(millstone.results().stream()
                            .limit(KaleidoscopeMillstoneRecipeData.MAX_RESULTS)
                            .map(output -> RecipeOutputData.of(output.stack(), output.chance()))
                            .toList());
            return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), KaleidoscopeCookeryRecipeEditorTypes.MILLSTONE).setData(data));
        }
        if (recipe instanceof ChoppingBoardRecipe choppingBoard) {
            var data = new KaleidoscopeChoppingBoardRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(choppingBoard.getIngredient()))
                    .setResult(RecipeImporter.copyResult(choppingBoard, provider))
                    .setCutCount(Math.max(1, choppingBoard.getCutCount()))
                    .setModelId(choppingBoard.getModelId());
            return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), KaleidoscopeCookeryRecipeEditorTypes.CHOPPING_BOARD).setData(data));
        }
        if (recipe instanceof SteamerRecipe steamer) {
            var data = new KaleidoscopeSteamerRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(steamer.getIngredient()))
                    .setResult(RecipeImporter.copyResult(steamer, provider))
                    .setCookTick(Math.max(1, steamer.getCookTick()));
            return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), KaleidoscopeCookeryRecipeEditorTypes.STEAMER).setData(data));
        }
        if (recipe instanceof TeapotRecipe teapot) {
            var data = new KaleidoscopeTeapotRecipeData()
                    .setTeaFluid(nonNullId(teapot.teaFluid(), WATER))
                    .setIngredient(RecipeImporter.importIngredient(teapot.ingredient())
                            .setCount(Math.max(1, teapot.ingredientCount())))
                    .setTime(Math.max(1, teapot.time()))
                    .setResult(RecipeImporter.copyResult(teapot, provider));
            return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), KaleidoscopeCookeryRecipeEditorTypes.TEAPOT).setData(data));
        }
        if (recipe instanceof BambooTrayRecipe bambooTray) {
            var data = new KaleidoscopeBambooTrayRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(bambooTray.getIngredient()))
                    .setResult(RecipeImporter.copyResult(bambooTray, provider))
                    .setSubtype(bambooTray.getSubtype().getSerializedName())
                    .setDuration(Math.max(1, bambooTray.getDuration()));
            return RecipeImporter.success(RecipeImporter.baseEntry(recipe.getId(), KaleidoscopeCookeryRecipeEditorTypes.BAMBOO_TRAY).setData(data));
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
