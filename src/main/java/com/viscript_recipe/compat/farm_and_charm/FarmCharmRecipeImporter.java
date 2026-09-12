package com.viscript_recipe.compat.farm_and_charm;

import com.viscript_recipe.compat.farm_and_charm.data.*;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.importer.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.satisfy.farm_and_charm.core.recipe.*;
import java.util.ArrayList;

/** Imports all six native categories, retaining containers, learning flags, experience, and alternatives. */
public final class FarmCharmRecipeImporter implements RecipeImportHandler {
    public static final FarmCharmRecipeImporter INSTANCE = new FarmCharmRecipeImporter();
    private FarmCharmRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && (holder.value() instanceof CookingPotRecipe || holder.value() instanceof StoveRecipe
                || holder.value() instanceof CraftingBowlRecipe || holder.value() instanceof RoasterRecipe
                || holder.value() instanceof MincerRecipe || holder.value() instanceof SiloRecipe);
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (!canImport(holder)) return null;
        var recipe = holder.value();
        var type = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
        var kind = FarmCharmRecipeKind.byType(type).orElseThrow();
        if (recipe.getIngredients().size() > kind.inputCount()) {
            throw new RecipeImportException("viscript_recipe.editor.farm_and_charm.too_many_inputs");
        }
        var inputs = new ArrayList<FarmCharmIngredientData>();
        for (var ingredient : recipe.getIngredients()) inputs.add(importIngredient(ingredient));
        var data = new FarmCharmRecipeData().setInputs(inputs).setResult(recipe.getResultItem(provider).copy());
        switch (recipe) {
            case CookingPotRecipe pot -> data.setContainerRequired(pot.isContainerRequired())
                    .setContainer(pot.getContainerItem().copy()).setRequiresLearning(pot.requiresLearning());
            case RoasterRecipe roaster -> data.setContainer(roaster.getContainer().copy()).setRequiresLearning(roaster.requiresLearning());
            case StoveRecipe stove -> data.setExperience(stove.getExperience()).setRequiresLearning(stove.requiresLearning());
            case MincerRecipe mincer -> data.setProcessingCategory(mincer.getRecipeType());
            case SiloRecipe silo -> data.setProcessingCategory(silo.getRecipeType());
            default -> { }
        }
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), type).setData(data));
    }

    private static FarmCharmIngredientData importIngredient(Ingredient ingredient) throws RecipeImportException {
        var data = new FarmCharmIngredientData();
        if (ingredient.isEmpty()) return data;
        if (ingredient.isCustom()) throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_ingredient");
        for (var value : ingredient.getValues()) {
            if (value instanceof Ingredient.ItemValue item) data.getAlternatives().add(RecipeIngredient.item(item.item().copy()));
            else if (value instanceof Ingredient.TagValue tag) data.getAlternatives().add(RecipeIngredient.tag(tag.tag().location()));
            else throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_ingredient");
        }
        return data;
    }
}
