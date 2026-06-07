package com.viscript_recipe.compat.irons_spellbooks;

import com.viscript_recipe.data.irons_spellbooks.IronSpellbooksRecipeEditorTypes;
import com.viscript_recipe.data.irons_spellbooks.IronAlchemistCauldronRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.EmptyAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.FillAlchemistCauldronRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;

public final class IronSpellbooksRecipeImporter implements RecipeImportHandler {
    public static final IronSpellbooksRecipeImporter INSTANCE = new IronSpellbooksRecipeImporter();

    private IronSpellbooksRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        if (holder == null || holder.value() == null) {
            return false;
        }
        var recipe = holder.value();
        return recipe instanceof FillAlchemistCauldronRecipe
                || recipe instanceof EmptyAlchemistCauldronRecipe
                || recipe instanceof BrewAlchemistCauldronRecipe;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        var recipe = holder.value();
        if (recipe instanceof FillAlchemistCauldronRecipe fill) {
            var data = new IronAlchemistCauldronRecipeData()
                    .setInput(RecipeImporter.importIngredient(fill.input()))
                    .setResult(RecipeImporter.copyStack(fill.returned()))
                    .setFluid(fill.result().copy())
                    .setMustFitAll(fill.mustFitAll())
                    .setSound(soundId(fill.fillSound(), ResourceLocation.withDefaultNamespace("item.bucket.empty")));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_FILL)
                    .setIronAlchemistCauldron(data));
        }
        if (recipe instanceof EmptyAlchemistCauldronRecipe empty) {
            var data = new IronAlchemistCauldronRecipeData()
                    .setInput(RecipeImporter.importIngredient(empty.input()))
                    .setResult(RecipeImporter.copyStack(empty.result()))
                    .setFluid(empty.fluid().copy())
                    .setSound(soundId(empty.emptySound(), ResourceLocation.withDefaultNamespace("item.bucket.fill")));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_EMPTY)
                    .setIronAlchemistCauldron(data));
        }
        if (recipe instanceof BrewAlchemistCauldronRecipe brew) {
            var results = new ArrayList<net.neoforged.neoforge.fluids.FluidStack>();
            for (var result : brew.results()) {
                if (result != null && !result.isEmpty()) {
                    results.add(result.copy());
                }
            }
            var data = new IronAlchemistCauldronRecipeData()
                    .setInput(RecipeImporter.importIngredient(brew.reagent()))
                    .setBaseFluid(brew.fluidIn().copy())
                    .setResultFluids(results)
                    .setByproduct(brew.byproduct().map(ItemStack::copy).orElse(ItemStack.EMPTY));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_BREW)
                    .setIronAlchemistCauldron(data));
        }
        return null;
    }

    private static ResourceLocation soundId(Holder<SoundEvent> holder, ResourceLocation fallback) {
        if (holder == null || holder.value() == null) {
            return fallback;
        }
        var id = BuiltInRegistries.SOUND_EVENT.getKey(holder.value());
        return id == null ? fallback : id;
    }
}
