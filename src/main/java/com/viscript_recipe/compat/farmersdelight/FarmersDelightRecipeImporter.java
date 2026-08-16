package com.viscript_recipe.compat.farmersdelight;

import com.viscript_recipe.compat.farmersdelight.data.FarmerCookingPotRecipeData;
import com.viscript_recipe.compat.farmersdelight.data.FarmerCuttingRecipeData;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.ingredient.ItemAbilityIngredient;

import java.util.ArrayList;

public final class FarmersDelightRecipeImporter implements RecipeImportHandler {
    public static final FarmersDelightRecipeImporter INSTANCE = new FarmersDelightRecipeImporter();

    private FarmersDelightRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        if (holder == null) {
            return false;
        }
        var recipe = holder.value();
        return recipe instanceof CookingPotRecipe || recipe instanceof CuttingBoardRecipe;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        var recipe = holder.value();
        if (recipe instanceof CookingPotRecipe cooking) {
            return RecipeImporter.success(importCooking(holder.id(), cooking, provider));
        }
        if (recipe instanceof CuttingBoardRecipe cutting) {
            return RecipeImporter.success(importCutting(holder.id(), cutting, provider));
        }
        return null;
    }

    private static RecipeEntry importCooking(ResourceLocation id, CookingPotRecipe recipe, HolderLookup.Provider provider) throws RecipeImportException {
        var data = new FarmerCookingPotRecipeData()
                .setIngredients(new ArrayList<>(RecipeImporter.importIngredientList(recipe.getIngredients(), CookingPotRecipe.INPUT_SLOTS)))
                .setResult(RecipeImporter.copyResult(recipe, provider))
                .setContainer(RecipeImporter.copyStack(recipe.getOutputContainer()))
                .setExperience(recipe.getExperience())
                .setCookingTime(Math.max(1, recipe.getCookTime()));
        return RecipeImporter.baseEntry(id, RecipeEditorTypes.FARMERSDELIGHT_COOKING).setData(data);
    }

    private static RecipeEntry importCutting(ResourceLocation id, CuttingBoardRecipe recipe, HolderLookup.Provider provider) throws RecipeImportException {
        var ingredients = recipe.getIngredients();
        if (ingredients.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
        }
        var results = new ArrayList<RecipeOutputData>();
        for (var result : recipe.getRollableResults()) {
            if (result != null && result.stack() != null && !result.stack().isEmpty()) {
                results.add(RecipeOutputData.of(result.stack().copy(), result.chance()));
            }
        }
        var sound = recipe.getSoundEvent()
                .map(BuiltInRegistries.SOUND_EVENT::getKey)
                .orElse(ResourceLocation.withDefaultNamespace("item.axe.strip"));
        var data = new FarmerCuttingRecipeData()
                .setInput(RecipeImporter.importIngredient(ingredients.getFirst()))
                .setTool(importTool(recipe.getTool()))
                .setResults(results)
                .setCustomSound(recipe.getSoundEvent().isPresent())
                .setSound(sound);
        if (data.getResults().isEmpty()) {
            data.getResults().add(RecipeOutputData.of(RecipeImporter.copyResult(recipe, provider)));
        }
        return RecipeImporter.baseEntry(id, RecipeEditorTypes.FARMERSDELIGHT_CUTTING).setData(data);
    }

    private static RecipeIngredient importTool(Ingredient ingredient) throws RecipeImportException {
        if (ingredient != null && ingredient.isCustom() && ingredient.getCustomIngredient() instanceof ItemAbilityIngredient itemAbility) {
            return RecipeIngredient.itemAbility(itemAbility.getItemAbility().name());
        }
        return RecipeImporter.importIngredient(ingredient);
    }
}
