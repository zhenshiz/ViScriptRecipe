package com.viscript_recipe.compat.forbidden_arcanus;

import com.stal111.forbidden_arcanus.common.block.entity.clibano.residue.ResidueType;
import com.stal111.forbidden_arcanus.common.item.crafting.ApplyModifierRecipe;
import com.stal111.forbidden_arcanus.common.item.crafting.ClibanoRecipe;
import com.stal111.forbidden_arcanus.common.item.enhancer.EnhancerDefinition;
import com.viscript_recipe.compat.forbidden_arcanus.data.ApplyModifierRecipeData;
import com.viscript_recipe.compat.forbidden_arcanus.data.ClibanoRecipeData;
import com.viscript_recipe.data.NativeIngredientData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;

import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeEditorTypes.APPLY_MODIFIER;
import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeEditorTypes.CLIBANO_COMBUSTION;
import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeValues.encode;

public enum ForbiddenArcanusRecipeImporter implements RecipeImportHandler {
    INSTANCE;

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder.value() instanceof ClibanoRecipe || holder.value() instanceof ApplyModifierRecipe;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        return switch (holder.value()) {
            case ClibanoRecipe recipe -> RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), CLIBANO_COMBUSTION)
                    .setData(importClibano(recipe, provider)));
            case ApplyModifierRecipe recipe -> RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), APPLY_MODIFIER)
                    .setData(new ApplyModifierRecipeData()
                            .setTemplate(NativeIngredientData.from(recipe.template(), provider))
                            .setAddition(NativeIngredientData.from(recipe.addition(), provider))
                            .setModifier(recipe.modifier().unwrapKey().orElseThrow().location())));
            default -> null;
        };
    }

    private static ClibanoRecipeData importClibano(ClibanoRecipe recipe, HolderLookup.Provider provider) throws RecipeImportException {
        var ingredients = recipe.getIngredients();
        var data = new ClibanoRecipeData().setInput(NativeIngredientData.from(ingredients.getFirst(), provider))
                .setResult(recipe.result().copy()).setFire(recipe.requiredFireType()).setGroup(recipe.group())
                .setCategory(recipe.category()).setExperience(recipe.experience()).setCookingTime(recipe.getDefaultCookingTime());
        if (recipe.isDoubleRecipe()) data.setSecondInput(NativeIngredientData.from(ingredients.get(1), provider));
        recipe.requiredEnhancer().ifPresent(holder -> data.setEnhancer(encode(EnhancerDefinition.REFERENCE_CODEC, provider, holder)));
        recipe.residueChance().ifPresent(chance -> data.setResidue(encode(ResidueType.CODEC, provider, chance.type()))
                .setResidueChance(chance.chance()));
        return data;
    }

}
