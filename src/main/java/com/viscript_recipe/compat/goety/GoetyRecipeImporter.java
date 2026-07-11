package com.viscript_recipe.compat.goety;

import com.Polarice3.Goety.common.crafting.BrazierRecipe;
import com.Polarice3.Goety.common.crafting.BrewingRecipe;
import com.Polarice3.Goety.common.crafting.CursedInfuserRecipes;
import com.Polarice3.Goety.common.crafting.PulverizeRecipe;
import com.Polarice3.Goety.common.crafting.RitualRecipe;
import com.viscript_recipe.data.goety.GoetyBrazierRecipeData;
import com.viscript_recipe.data.goety.GoetyBrewingEntityKind;
import com.viscript_recipe.data.goety.GoetyBrewingRecipeData;
import com.viscript_recipe.data.goety.GoetyCursedInfuserRecipeData;
import com.viscript_recipe.data.goety.GoetyPulverizeRecipeData;
import com.viscript_recipe.data.goety.GoetyPulverizeResultKind;
import com.viscript_recipe.data.goety.GoetyRecipeEditorTypes;
import com.viscript_recipe.data.goety.GoetyRitualCraftType;
import com.viscript_recipe.data.goety.GoetyRitualRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;

/**
 * Imports Goety's five JEI-backed recipe classes into editor-owned data.
 */
public final class GoetyRecipeImporter implements RecipeImportHandler {
    public static final GoetyRecipeImporter INSTANCE = new GoetyRecipeImporter();

    private GoetyRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && (holder.value() instanceof CursedInfuserRecipes
                || holder.value() instanceof RitualRecipe
                || holder.value() instanceof BrazierRecipe
                || holder.value() instanceof PulverizeRecipe
                || holder.value() instanceof BrewingRecipe);
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (holder.value() instanceof CursedInfuserRecipes recipe) {
            var data = new GoetyCursedInfuserRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(recipe.getIngredients().getFirst()))
                    .setResult(RecipeImporter.copyResult(recipe, provider))
                    .setCookingTime(Math.max(1, recipe.getCookingTime()))
                    .setGrim(recipe.isGrim());
            return success(holder, GoetyRecipeEditorTypes.CURSED_INFUSER_RECIPE, entry -> entry.setGoetyCursedInfuser(data));
        }
        if (holder.value() instanceof RitualRecipe recipe) {
            var enchantmentId = recipe.getEnchantmentHolder() == null
                    ? null
                    : recipe.getEnchantmentHolder().unwrapKey().map(key -> key.location()).orElse(null);
            var data = new GoetyRitualRecipeData()
                    .setActivationItem(RecipeImporter.importIngredient(recipe.getActivationItem()))
                    .setIngredients(RecipeImporter.importIngredientList(recipe.getIngredients(), GoetyRitualRecipeData.MAX_PEDESTAL_INGREDIENTS))
                    .setResult(RecipeImporter.copyResult(recipe, provider))
                    .setCraftType(GoetyRitualCraftType.byName(recipe.getCraftType()))
                    .setRitualType(recipe.getRitualType())
                    .setSoulCost(Math.max(0, recipe.getSoulCost()))
                    .setDuration(Math.max(1, recipe.getDuration()))
                    .setSummonLife(recipe.getSummonLife())
                    .setHasSacrifice(recipe.getEntityToSacrifice() != null)
                    .setEntityToSacrifice(recipe.getEntityToSacrifice() == null ? null : recipe.getEntityToSacrifice().location())
                    .setEntityToSacrificeDisplayName(recipe.getEntityToSacrificeDisplayName())
                    .setHasSummon(recipe.getEntityToSummon() != null)
                    .setEntityToSummon(entityId(recipe.getEntityToSummon()))
                    .setHasConversion(recipe.getEntityToConvert() != null || recipe.getEntityToConvertInto() != null)
                    .setEntityToConvert(recipe.getEntityToConvert() == null ? null : recipe.getEntityToConvert().location())
                    .setEntityToConvertDisplayName(recipe.getEntityToConvertDisplayName())
                    .setEntityToConvertInto(entityId(recipe.getEntityToConvertInto()))
                    .setHasStructure(recipe.getStructureTag() != null)
                    .setStructureToLocate(recipe.getStructureTag() == null ? null : recipe.getStructureTag().location())
                    .setStructureDisplayName(recipe.getStructureName())
                    .setHasEnchantment(enchantmentId != null)
                    .setEnchantment(enchantmentId)
                    .setXpLevelCost(Math.max(0, recipe.getXPLevelCost()))
                    .setResearch(recipe.getResearch());
            return success(holder, GoetyRecipeEditorTypes.RITUAL, entry -> entry.setGoetyRitual(data));
        }
        if (holder.value() instanceof BrazierRecipe recipe) {
            var data = new GoetyBrazierRecipeData()
                    .setIngredients(RecipeImporter.importIngredientList(recipe.getIngredients(), GoetyBrazierRecipeData.INPUT_COUNT))
                    .setResult(RecipeImporter.copyResult(recipe, provider))
                    .setSoulCost(Math.max(0, recipe.getSoulCost()));
            return success(holder, GoetyRecipeEditorTypes.BRAZIER, entry -> entry.setGoetyBrazier(data));
        }
        if (holder.value() instanceof PulverizeRecipe recipe) {
            var itemResult = RecipeImporter.copyResult(recipe, provider);
            var blockResult = recipe.getBlockResult();
            var itemMode = !itemResult.isEmpty();
            var data = new GoetyPulverizeRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(recipe.getIngredients().getFirst()))
                    .setResultKind(itemMode ? GoetyPulverizeResultKind.ITEM : GoetyPulverizeResultKind.BLOCK)
                    .setItemResult(itemResult)
                    .setBlockResult(blockResult == null || blockResult == Blocks.CAVE_AIR
                            ? ResourceLocation.withDefaultNamespace("cobblestone")
                            : BuiltInRegistries.BLOCK.getKey(blockResult));
            return success(holder, GoetyRecipeEditorTypes.PULVERIZE, entry -> entry.setGoetyPulverize(data));
        }
        if (holder.value() instanceof BrewingRecipe recipe) {
            var entityKind = recipe.getEntityTypeTag() != null
                    ? GoetyBrewingEntityKind.TAG
                    : recipe.getEntityType() != null ? GoetyBrewingEntityKind.ENTITY : GoetyBrewingEntityKind.NONE;
            var entity = recipe.getEntityTypeTag() != null
                    ? recipe.getEntityTypeTag().location()
                    : entityId(recipe.getEntityType());
            var data = new GoetyBrewingRecipeData()
                    .setIngredient(RecipeImporter.importIngredient(recipe.getInput()))
                    .setEffect(BuiltInRegistries.MOB_EFFECT.getKey(recipe.getOutput()))
                    .setSoulCost(Math.max(0, recipe.getSoulCost()))
                    .setCapacityExtra(Math.max(0, recipe.getCapacityExtra()))
                    .setDuration(Math.max(1, recipe.getDuration()))
                    .setEntityKind(entityKind)
                    .setEntity(entity);
            return success(holder, GoetyRecipeEditorTypes.BREWING, entry -> entry.setGoetyBrewing(data));
        }
        return null;
    }

    private static RecipeImportResult success(RecipeHolder<?> holder, ResourceLocation type,
                                              java.util.function.Consumer<com.viscript_recipe.data.RecipeEntry> consumer) {
        var entry = RecipeImporter.baseEntry(holder.id(), type);
        consumer.accept(entry);
        return RecipeImporter.success(entry);
    }

    private static ResourceLocation entityId(net.minecraft.world.entity.EntityType<?> entityType) {
        return entityType == null ? null : BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
    }
}
