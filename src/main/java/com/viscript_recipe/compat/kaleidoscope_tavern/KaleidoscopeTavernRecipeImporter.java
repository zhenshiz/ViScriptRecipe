package com.viscript_recipe.compat.kaleidoscope_tavern;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.PressingTubRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.ShakerRecipe;
import com.viscript_recipe.compat.kaleidoscope_tavern.data.KaleidoscopeBarrelRecipeData;
import com.viscript_recipe.compat.kaleidoscope_tavern.data.KaleidoscopePressingTubRecipeData;
import com.viscript_recipe.compat.kaleidoscope_tavern.data.KaleidoscopeShakerRecipeData;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;

public final class KaleidoscopeTavernRecipeImporter implements RecipeImportHandler {
    public static final KaleidoscopeTavernRecipeImporter INSTANCE = new KaleidoscopeTavernRecipeImporter();

    private static final ResourceLocation WATER = ResourceLocation.withDefaultNamespace("water");

    private KaleidoscopeTavernRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        if (holder == null) {
            return false;
        }
        var recipe = holder.value();
        return recipe instanceof BarrelRecipe
                || recipe instanceof PressingTubRecipe
                || recipe instanceof ShakerRecipe;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        var recipe = holder.value();
        if (recipe instanceof BarrelRecipe barrel) {
            var data = new KaleidoscopeBarrelRecipeData()
                    .setIngredients(new ArrayList<>(RecipeImporter.importIngredientList(barrel.ingredients(), KaleidoscopeTavernRecipeFactory.BARREL_MAX_INGREDIENTS)))
                    .setFluid(nonNullId(KaleidoscopeTavernRecipeFactory.fluidId(barrel.fluid())))
                    .setCarrier(importOptional(barrel.carrier()))
                    .setResult(RecipeImporter.copyResult(barrel, provider))
                    .setUnitTime(Math.max(1, barrel.unitTime()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.KALEIDOSCOPE_TAVERN_BARREL).setData(data));
        }
        if (recipe instanceof PressingTubRecipe pressingTub) {
            var data = new KaleidoscopePressingTubRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(pressingTub.getIngredient()))
                    .setFluid(nonNullId(KaleidoscopeTavernRecipeFactory.fluidId(pressingTub.getFluid())))
                    .setFluidAmount(Math.max(1, pressingTub.getFluidAmount()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.KALEIDOSCOPE_TAVERN_PRESSING_TUB).setData(data));
        }
        if (recipe instanceof ShakerRecipe shaker) {
            var data = new KaleidoscopeShakerRecipeData()
                    .setIngredients(new ArrayList<>(RecipeImporter.importIngredientList(shaker.ingredients(), KaleidoscopeTavernRecipeFactory.SHAKER_MAX_INGREDIENTS)))
                    .setResult(RecipeImporter.copyResult(shaker, provider));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.KALEIDOSCOPE_TAVERN_SHAKER).setData(data));
        }
        return null;
    }

    private static RecipeIngredient importOptional(Ingredient ingredient) throws RecipeImportException {
        return ingredient == null || ingredient.isEmpty() ? RecipeIngredient.empty() : RecipeImporter.importIngredient(ingredient);
    }

    private static ResourceLocation nonNullId(ResourceLocation id) {
        return id == null ? WATER : id;
    }
}
