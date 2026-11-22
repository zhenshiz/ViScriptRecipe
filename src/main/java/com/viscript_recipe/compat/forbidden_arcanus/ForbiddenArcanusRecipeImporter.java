package com.viscript_recipe.compat.forbidden_arcanus;

import com.stal111.forbidden_arcanus.common.block.entity.clibano.residue.ResidueType;
import com.stal111.forbidden_arcanus.common.block.entity.forge.circle.MagicCircleType;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.Ritual;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.CreateItemResult;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.TransmuteInputResult;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.UpgradeTierResult;
import com.stal111.forbidden_arcanus.common.item.crafting.ApplyModifierRecipe;
import com.stal111.forbidden_arcanus.common.item.crafting.ClibanoRecipe;
import com.stal111.forbidden_arcanus.common.item.enhancer.EnhancerDefinition;
import com.viscript_recipe.compat.forbidden_arcanus.data.ApplyModifierRecipeData;
import com.viscript_recipe.compat.forbidden_arcanus.data.ClibanoRecipeData;
import com.viscript_recipe.compat.forbidden_arcanus.data.ForgeRitualRecipeData;
import com.viscript_recipe.data.NativeIngredientData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;

import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeEditorTypes.*;
import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeValues.encode;

public enum ForbiddenArcanusRecipeImporter implements RecipeImportHandler {
    INSTANCE;

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder.value() instanceof ClibanoRecipe || holder.value() instanceof ApplyModifierRecipe
                || holder.value() instanceof ForgeRitualRecipe ritual && ForgeRitualRecipes.isEditable(ritual.ritual());
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
            case ForgeRitualRecipe recipe -> RecipeImporter.success(RecipeImporter.baseEntry(ForgeRitualRecipes.ritualId(holder.id()),
                            recipe.ritual().result() instanceof UpgradeTierResult ? HEPHAESTUS_UPGRADING : HEPHAESTUS_SMITHING)
                    .setData(importRitual(recipe.ritual(), provider)));
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

    private static ForgeRitualRecipeData importRitual(Ritual ritual, HolderLookup.Provider provider) throws RecipeImportException {
        var requirements = ritual.requirements();
        var essence = requirements.essences();
        var data = new ForgeRitualRecipeData()
                .setCenter(NativeIngredientData.from(ritual.mainIngredient(), provider))
                .setForgeTier(requirements.tier().tier()).setExactTier(requirements.tier().matchExact())
                .setAureal(essence.aureal()).setSouls(essence.souls()).setBlood(essence.blood()).setExperience(essence.experience())
                .setDuration(ritual.duration()).setMagicCircle(encode(MagicCircleType.CODEC, provider, ritual.magicCircleType()));
        var enhancers = requirements.enhancers();
        enhancers.unwrapKey().ifPresentOrElse(tag -> data.setEnhancerTag(tag.location().toString()),
                () -> data.setEnhancers(new ArrayList<>(enhancers.stream()
                        .map(holder -> encode(EnhancerDefinition.REFERENCE_CODEC, provider, holder)).toList())));

        int index = 0;
        for (var input : ritual.inputs()) {
            if (input.amount() > ForgeRitualRecipeData.PEDESTAL_COUNT - index) {
                throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_ingredients",
                        (long) index + input.amount(), ForgeRitualRecipeData.PEDESTAL_COUNT);
            }
            for (int i = 0; i < input.amount(); i++) {
                data.getPedestals().set(index++, NativeIngredientData.from(input.ingredient(), provider));
            }
        }
        switch (ritual.result()) {
            case CreateItemResult result -> data.setResult(result.result().copy());
            case TransmuteInputResult result -> data.setTransmuteInput(true).setResult(result.result().value().getDefaultInstance());
            case UpgradeTierResult result -> data.setResultTier(result.resultTier());
            default -> throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_type",
                    HEPHAESTUS_SMITHING.toString());
        }
        return data;
    }
}
