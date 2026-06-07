package com.viscript_recipe.compat.avaritia;

import committee.nova.mods.avaritia.common.crafting.recipe.CompressorRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.EternalSingularityCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ExtremeSmithingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.FullMatterClusterRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.InfinityCatalystCraftRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.NoConsumeCatalystShapedRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapedTableCraftingRecipe;
import committee.nova.mods.avaritia.common.crafting.recipe.ShapelessTableCraftingRecipe;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.avaritia.AvaritiaCompressorRecipeData;
import com.viscript_recipe.data.avaritia.AvaritiaExtremeSmithingRecipeData;
import com.viscript_recipe.data.avaritia.AvaritiaRecipeEditorTypes;
import com.viscript_recipe.data.avaritia.AvaritiaTableRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;

import java.util.ArrayList;
import java.util.List;

public final class AvaritiaRecipeImporter implements RecipeImportHandler {
    public static final AvaritiaRecipeImporter INSTANCE = new AvaritiaRecipeImporter();

    private AvaritiaRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        if (holder == null || holder.value() == null) {
            return false;
        }
        var recipe = holder.value();
        return recipe instanceof NoConsumeCatalystShapedRecipe
                || recipe instanceof ShapedTableCraftingRecipe
                || recipe instanceof ShapelessTableCraftingRecipe && !isSpecialShapeless(recipe)
                || recipe instanceof CompressorRecipe
                || recipe instanceof ExtremeSmithingRecipe;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        var recipe = holder.value();
        if (recipe instanceof NoConsumeCatalystShapedRecipe noConsume) {
            return RecipeImporter.success(importShapedTable(holder, provider, noConsume, AvaritiaRecipeEditorTypes.NO_CONSUME_CATALYST_SHAPED, false));
        }
        if (recipe instanceof ShapedTableCraftingRecipe shaped) {
            return RecipeImporter.success(importShapedTable(holder, provider, shaped, AvaritiaRecipeEditorTypes.SHAPED_TABLE, shaped.isCompatible()));
        }
        if (recipe instanceof ShapelessTableCraftingRecipe shapeless && !isSpecialShapeless(shapeless)) {
            var tier = Math.max(1, shapeless.getTier());
            var gridSize = AvaritiaRecipeEditorTypes.tableGridSizeForTier(tier);
            var data = new AvaritiaTableRecipeData()
                    .setTier(tier)
                    .setWidth(gridSize)
                    .setHeight(gridSize)
                    .setShapelessIngredients(new ArrayList<>(RecipeImporter.importIngredientList(shapeless.getIngredients(), 81)))
                    .setResult(RecipeImporter.copyResult(shapeless, provider));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), AvaritiaRecipeEditorTypes.SHAPELESS_TABLE).setAvaritiaTable(data));
        }
        if (recipe instanceof CompressorRecipe compressor) {
            var data = new AvaritiaCompressorRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(compressor.getInput()))
                    .setResult(RecipeImporter.copyResult(compressor, provider))
                    .setInputCount(Math.max(1, compressor.getInputCount()))
                    .setTimeCost(Math.max(1, compressor.getTimeCost()));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), AvaritiaRecipeEditorTypes.COMPRESSOR).setAvaritiaCompressor(data));
        }
        if (recipe instanceof ExtremeSmithingRecipe smithing) {
            var data = new AvaritiaExtremeSmithingRecipeData()
                    .setTemplate(RecipeImporter.importIngredient(smithing.template))
                    .setBase(RecipeImporter.importIngredient(smithing.base))
                    .setAdditions(importAdditions(smithing.additions))
                    .setResult(RecipeImporter.copyResult(smithing, provider));
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), AvaritiaRecipeEditorTypes.EXTREME_SMITHING).setAvaritiaExtremeSmithing(data));
        }
        return null;
    }

    private static com.viscript_recipe.data.RecipeEntry importShapedTable(RecipeHolder<?> holder, HolderLookup.Provider provider,
                                                                          ShapedTableCraftingRecipe recipe,
                                                                          net.minecraft.resources.ResourceLocation type,
                                                                          boolean compatible) throws RecipeImportException {
        var pattern = RecipeImporter.importShapedPattern(recipe.getIngredients(), recipe.getWidth(), recipe.getHeight());
        var data = new AvaritiaTableRecipeData()
                .setWidth(recipe.getWidth())
                .setHeight(recipe.getHeight())
                .setTier(Math.max(1, recipe.getTier()))
                .setCompatible(compatible)
                .setPattern(pattern.pattern())
                .setKey(pattern.key())
                .setResult(RecipeImporter.copyResult(recipe, provider));
        return RecipeImporter.baseEntry(holder.id(), type).setAvaritiaTable(data);
    }

    private static List<RecipeIngredient> importAdditions(Ingredient additions) throws RecipeImportException {
        var imported = new ArrayList<RecipeIngredient>();
        if (additions != null && additions.isCustom() && additions.getCustomIngredient() instanceof CompoundIngredient compound) {
            for (var child : compound.children()) {
                if (child != null && !child.isEmpty()) {
                    imported.add(RecipeImporter.importIngredient(child));
                }
            }
        } else if (additions != null && !additions.isEmpty()) {
            imported.add(RecipeImporter.importIngredient(additions));
        }
        while (imported.size() < 3) {
            imported.add(new RecipeIngredient());
        }
        return imported.stream().limit(3).toList();
    }

    private static boolean isSpecialShapeless(Object recipe) {
        return recipe instanceof InfinityCatalystCraftRecipe
                || recipe instanceof EternalSingularityCraftRecipe
                || recipe instanceof FullMatterClusterRecipe;
    }
}
