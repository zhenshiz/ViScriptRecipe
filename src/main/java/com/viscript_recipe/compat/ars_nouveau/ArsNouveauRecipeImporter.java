package com.viscript_recipe.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.common.crafting.recipes.ArmorUpgradeRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.CrushRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.EnchantmentRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.GlyphRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ImbuementRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.PrestidigitationRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ReactiveEnchantmentRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.SpellWriteRecipe;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.ars_nouveau.ArsNouveauApparatusRecipeData;
import com.viscript_recipe.data.ars_nouveau.ArsNouveauArmorUpgradeRecipeData;
import com.viscript_recipe.data.ars_nouveau.ArsNouveauCrushOutputData;
import com.viscript_recipe.data.ars_nouveau.ArsNouveauCrushRecipeData;
import com.viscript_recipe.data.ars_nouveau.ArsNouveauEnchantmentRecipeData;
import com.viscript_recipe.data.ars_nouveau.ArsNouveauGlyphRecipeData;
import com.viscript_recipe.data.ars_nouveau.ArsNouveauImbuementRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;

public final class ArsNouveauRecipeImporter implements RecipeImportHandler {
    public static final ArsNouveauRecipeImporter INSTANCE = new ArsNouveauRecipeImporter();

    private ArsNouveauRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        if (holder == null || holder.value() == null) {
            return false;
        }
        var recipe = holder.value();
        return recipe instanceof ImbuementRecipe
                || recipe instanceof GlyphRecipe
                || recipe instanceof CrushRecipe
                || recipe instanceof ArmorUpgradeRecipe
                || (recipe instanceof EnchantmentRecipe && !(recipe instanceof ReactiveEnchantmentRecipe))
                || (recipe instanceof EnchantingApparatusRecipe && !isPedestalOnly(recipe));
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        var recipe = holder.value();
        if (recipe instanceof ArmorUpgradeRecipe armorUpgrade) {
            var data = new ArsNouveauArmorUpgradeRecipeData()
                    .setPedestalItems(new ArrayList<>(RecipeImporter.importIngredientList(armorUpgrade.pedestalItems(), 8)))
                    .setSourceCost(Math.max(0, armorUpgrade.sourceCost()))
                    .setTier(Math.max(1, armorUpgrade.tier()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.ARS_NOUVEAU_ARMOR_UPGRADE).setArsNouveauArmorUpgrade(data));
        }
        if (recipe instanceof EnchantmentRecipe enchantment && !(recipe instanceof ReactiveEnchantmentRecipe)) {
            var data = new ArsNouveauEnchantmentRecipeData()
                    .setPedestalItems(new ArrayList<>(RecipeImporter.importIngredientList(enchantment.pedestalItems(), 8)))
                    .setEnchantment(enchantment.enchantmentKey().location())
                    .setLevel(Math.max(1, enchantment.enchantLevel()))
                    .setSourceCost(Math.max(0, enchantment.sourceCost()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.ARS_NOUVEAU_ENCHANTMENT).setArsNouveauEnchantment(data));
        }
        if (recipe instanceof EnchantingApparatusRecipe apparatus && !isPedestalOnly(apparatus)) {
            var data = new ArsNouveauApparatusRecipeData()
                    .setReagent(RecipeImporter.importIngredient(apparatus.reagent()))
                    .setPedestalItems(new ArrayList<>(RecipeImporter.importIngredientList(apparatus.pedestalItems(), 8)))
                    .setResult(RecipeImporter.copyResult(apparatus, provider))
                    .setSourceCost(Math.max(0, apparatus.sourceCost()))
                    .setKeepNbtOfReagent(apparatus.keepNbtOfReagent());
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.ARS_NOUVEAU_ENCHANTING_APPARATUS).setArsNouveauApparatus(data));
        }
        if (recipe instanceof ImbuementRecipe imbuement) {
            var data = new ArsNouveauImbuementRecipeData()
                    .setInput(RecipeImporter.importIngredient(imbuement.getInput()))
                    .setPedestalItems(new ArrayList<>(RecipeImporter.importIngredientList(imbuement.getPedestalItems(), 8)))
                    .setResult(RecipeImporter.copyResult(imbuement, provider))
                    .setSource(Math.max(0, imbuement.getSource()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.ARS_NOUVEAU_IMBUEMENT).setArsNouveauImbuement(data));
        }
        if (recipe instanceof GlyphRecipe glyph) {
            var data = new ArsNouveauGlyphRecipeData()
                    .setInputs(new ArrayList<>(RecipeImporter.importIngredientList(glyph.getInputs(), 9)))
                    .setResult(RecipeImporter.copyResult(glyph, provider))
                    .setExp(Math.max(0, glyph.getExp()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.ARS_NOUVEAU_GLYPH).setArsNouveauGlyph(data));
        }
        if (recipe instanceof CrushRecipe crush) {
            var outputs = new ArrayList<ArsNouveauCrushOutputData>();
            for (var output : crush.outputs()) {
                if (output != null && output.stack() != null && !output.stack().isEmpty()) {
                    outputs.add(new ArsNouveauCrushOutputData()
                            .setItem(output.stack().copy())
                            .setChance(output.chance())
                            .setMaxRange(Math.max(1, output.maxRange())));
                }
            }
            var data = new ArsNouveauCrushRecipeData()
                    .setInput(RecipeImporter.importIngredient(crush.input()))
                    .setOutputs(outputs)
                    .setSkipBlockPlace(crush.skipBlockPlace());
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), RecipeEditorTypes.ARS_NOUVEAU_CRUSH).setArsNouveauCrush(data));
        }
        return null;
    }

    private static boolean isPedestalOnly(Object recipe) {
        return recipe instanceof ReactiveEnchantmentRecipe
                || recipe instanceof SpellWriteRecipe
                || recipe instanceof PrestidigitationRecipe;
    }
}
