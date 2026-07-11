package com.viscript_recipe.compat.cataclysm;

import com.github.L_Ender.cataclysm.crafting.AltarOfAmethystRecipe;
import com.github.L_Ender.cataclysm.crafting.WeaponfusionRecipe;
import com.viscript_recipe.data.cataclysm.CataclysmAmethystBlessRecipeData;
import com.viscript_recipe.data.cataclysm.CataclysmRecipeEditorTypes;
import com.viscript_recipe.data.cataclysm.CataclysmWeaponFusionRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * Imports Cataclysm's two JEI-backed custom recipe types into explicit editor data.
 */
public final class CataclysmRecipeImporter implements RecipeImportHandler {
    public static final CataclysmRecipeImporter INSTANCE = new CataclysmRecipeImporter();

    private CataclysmRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && (holder.value() instanceof WeaponfusionRecipe
                || holder.value() instanceof AltarOfAmethystRecipe);
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (holder.value() instanceof WeaponfusionRecipe recipe) {
            var data = new CataclysmWeaponFusionRecipeData()
                    .setBase(RecipeImporter.importIngredient(recipe.getbaseIngredient()))
                    .setAddition(RecipeImporter.importIngredient(recipe.getAdditionIngredient()))
                    .setResult(RecipeImporter.copyResult(recipe, provider));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), CataclysmRecipeEditorTypes.WEAPON_FUSION)
                    .setCataclysmWeaponFusion(data));
        }
        if (holder.value() instanceof AltarOfAmethystRecipe recipe) {
            var data = new CataclysmAmethystBlessRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(recipe.getIngredients().getFirst()))
                    .setResult(RecipeImporter.copyResult(recipe, provider))
                    .setTime(Math.max(1, recipe.getTime()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), CataclysmRecipeEditorTypes.AMETHYST_BLESS)
                    .setCataclysmAmethystBless(data));
        }
        return null;
    }
}
