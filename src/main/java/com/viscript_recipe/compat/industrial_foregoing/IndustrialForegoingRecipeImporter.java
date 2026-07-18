package com.viscript_recipe.compat.industrial_foregoing;

import com.buuz135.industrial.recipe.*;
import com.buuz135.industrial.recipe.data.EntityData;
import com.viscript_recipe.data.industrial_foregoing.*;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SingleFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.TagFluidIngredient;

import java.util.ArrayList;
import java.util.function.Consumer;

/** Imports Industrial Foregoing recipes backed by native codecs. */
public final class IndustrialForegoingRecipeImporter implements RecipeImportHandler {
    public static final IndustrialForegoingRecipeImporter INSTANCE = new IndustrialForegoingRecipeImporter();

    private IndustrialForegoingRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && (holder.value() instanceof CrusherRecipe
                || holder.value() instanceof DissolutionChamberRecipe
                || holder.value() instanceof FluidExtractorRecipe
                || holder.value() instanceof LaserDrillOreRecipe
                || holder.value() instanceof LaserDrillFluidRecipe
                || holder.value() instanceof StoneWorkGenerateRecipe);
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (holder.value() instanceof CrusherRecipe recipe) {
            var data = new IndustrialCrusherRecipeData()
                    .setInput(RecipeImporter.importIngredient(recipe.input))
                    .setOutput(RecipeImporter.importIngredient(recipe.output));
            return success(holder, IndustrialForegoingRecipeEditorTypes.CRUSHER,
                    entry -> entry.setData(data));
        }
        if (holder.value() instanceof DissolutionChamberRecipe recipe) {
            var data = new IndustrialDissolutionRecipeData()
                    .setInput(RecipeImporter.importIngredientList(recipe.input, IndustrialDissolutionRecipeData.MAX_INPUTS))
                    .setInputFluid(importFluidIngredient(recipe.inputFluid))
                    .setProcessingTime(Math.max(0, recipe.processingTime))
                    .setHasItemOutput(recipe.output.isPresent())
                    .setOutput(recipe.output.map(RecipeImporter::copyStack).orElse(net.minecraft.world.item.ItemStack.EMPTY))
                    .setHasFluidOutput(recipe.outputFluid.isPresent())
                    .setOutputFluid(recipe.outputFluid.map(FluidStack::copy).orElse(FluidStack.EMPTY));
            return success(holder, IndustrialForegoingRecipeEditorTypes.DISSOLUTION_CHAMBER,
                    entry -> entry.setData(data));
        }
        if (holder.value() instanceof FluidExtractorRecipe recipe) {
            var data = new IndustrialFluidExtractorRecipeData()
                    .setInput(RecipeImporter.importIngredient(recipe.input))
                    .setResultBlock(BuiltInRegistries.BLOCK.getKey(recipe.result.getBlock()))
                    .setResultProperties(importBlockState(recipe.result))
                    .setBreakChance(recipe.breakChance)
                    .setOutput(recipe.output.copy())
                    .setDefaultRecipe(recipe.defaultRecipe);
            return success(holder, IndustrialForegoingRecipeEditorTypes.FLUID_EXTRACTOR,
                    entry -> entry.setData(data));
        }
        if (holder.value() instanceof LaserDrillOreRecipe recipe) {
            var data = new IndustrialLaserDrillOreRecipeData()
                    .setOutput(RecipeImporter.importIngredient(recipe.output.ingredient()))
                    .setOutputCount(Math.max(1, recipe.output.count()))
                    .setCatalyst(RecipeImporter.importIngredient(recipe.catalyst))
                    .setEntityCondition(importEntityCondition(recipe.entityData.orElse(null)))
                    .setRarity(recipe.rarity.stream().map(IndustrialForegoingRecipeImporter::importRarity).collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
            return success(holder, IndustrialForegoingRecipeEditorTypes.LASER_DRILL_ORE,
                    entry -> entry.setData(data));
        }
        if (holder.value() instanceof LaserDrillFluidRecipe recipe) {
            var data = new IndustrialLaserDrillFluidRecipeData()
                    .setOutput(importFluidIngredient(recipe.output))
                    .setCatalyst(RecipeImporter.importIngredient(recipe.catalyst))
                    .setEntityCondition(importEntityCondition(recipe.entityData.orElse(null)))
                    .setRarity(recipe.rarity.stream().map(IndustrialForegoingRecipeImporter::importRarity).collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
            return success(holder, IndustrialForegoingRecipeEditorTypes.LASER_DRILL_FLUID,
                    entry -> entry.setData(data));
        }
        if (holder.value() instanceof StoneWorkGenerateRecipe recipe) {
            var data = new IndustrialStoneWorkRecipeData()
                    .setOutput(RecipeImporter.copyStack(recipe.output))
                    .setWaterNeed(Math.max(0, recipe.waterNeed))
                    .setLavaNeed(Math.max(0, recipe.lavaNeed))
                    .setWaterConsume(Math.max(0, recipe.waterConsume))
                    .setLavaConsume(Math.max(0, recipe.lavaConsume));
            return success(holder, IndustrialForegoingRecipeEditorTypes.STONEWORK_GENERATE,
                    entry -> entry.setData(data));
        }
        return null;
    }

    private static IndustrialFluidIngredientData importFluidIngredient(SizedFluidIngredient ingredient) throws RecipeImportException {
        var data = new IndustrialFluidIngredientData().setAmount(Math.max(1, ingredient.amount()));
        if (ingredient.ingredient() instanceof TagFluidIngredient tagIngredient) {
            return data.setKind(IndustrialFluidIngredientKind.TAG).setTag(tagIngredient.tag().location());
        }
        if (ingredient.ingredient() instanceof SingleFluidIngredient singleFluid) {
            return data.setKind(IndustrialFluidIngredientKind.FLUID)
                    .setFluid(new FluidStack(singleFluid.fluid().value(), data.getAmount()));
        }
        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_fluid_ingredient");
    }

    private static IndustrialEntityConditionData importEntityCondition(EntityData entityData) {
        var data = new IndustrialEntityConditionData();
        if (entityData == null) {
            return data.setEnabled(false);
        }
        var ingredient = entityData.getEntity();
        if (ingredient.isTag()) {
            data.setKind(IndustrialEntityIngredientKind.TAG).setId(ingredient.tag().location());
        } else {
            data.setKind(IndustrialEntityIngredientKind.ENTITY)
                    .setId(BuiltInRegistries.ENTITY_TYPE.getKey(ingredient.getType()));
        }
        return data.setEnabled(true)
                .setNbt(entityData.getData().toString())
                .setDisplay(entityData.getDisplay().getString());
    }

    private static IndustrialLaserDrillRarityData importRarity(LaserDrillRarity rarity) {
        return new IndustrialLaserDrillRarityData()
                .setBiomeWhitelist(rarity.biomeRarity().whitelist().stream().map(TagKey::location).collect(java.util.stream.Collectors.toCollection(ArrayList::new)))
                .setBiomeBlacklist(rarity.biomeRarity().blacklist().stream().map(TagKey::location).collect(java.util.stream.Collectors.toCollection(ArrayList::new)))
                .setDimensionWhitelist(rarity.dimensionRarity().whitelist().stream().map(ResourceKey::location).collect(java.util.stream.Collectors.toCollection(ArrayList::new)))
                .setDimensionBlacklist(rarity.dimensionRarity().blacklist().stream().map(ResourceKey::location).collect(java.util.stream.Collectors.toCollection(ArrayList::new)))
                .setDepthMin(rarity.depth_min())
                .setDepthMax(rarity.depth_max())
                .setWeight(Math.max(1, rarity.weight()));
    }

    private static ArrayList<IndustrialBlockStatePropertyData> importBlockState(BlockState state) {
        var result = new ArrayList<IndustrialBlockStatePropertyData>();
        for (var entry : state.getValues().entrySet()) {
            result.add(new IndustrialBlockStatePropertyData()
                    .setName(entry.getKey().getName())
                    .setValue(propertyValueName(entry.getKey(), entry.getValue())));
        }
        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static String propertyValueName(Property property, Comparable value) {
        return property.getName(value);
    }

    private static RecipeImportResult success(RecipeHolder<?> holder, ResourceLocation type,
                                              Consumer<com.viscript_recipe.data.RecipeEntry> consumer) {
        var entry = RecipeImporter.baseEntry(holder.id(), type);
        consumer.accept(entry);
        return RecipeImporter.success(entry);
    }
}
