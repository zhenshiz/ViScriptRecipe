package com.viscript_recipe.compat.immersive_engineering;

import blusunrize.immersiveengineering.api.crafting.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.RegistryOps;
import com.viscript_recipe.compat.immersive_engineering.data.IERecipeData;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SingleFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.TagFluidIngredient;

import java.util.ArrayList;

/** Imports native IE recipes while keeping their machine-specific parameters editable. */
public final class IERecipeImporter implements RecipeImportHandler {
    public static final IERecipeImporter INSTANCE = new IERecipeImporter();
    private IERecipeImporter() {}

    @Override public boolean canImport(RecipeHolder<?> holder) {
        if (holder == null) return false;
        var recipe = holder.value();
        return recipe instanceof AlloyRecipe || recipe instanceof ArcFurnaceRecipe || recipe instanceof BlastFurnaceFuel
                || recipe instanceof BlastFurnaceRecipe || recipe instanceof BlueprintCraftingRecipe
                || recipe instanceof BottlingMachineRecipe || recipe instanceof ClocheFertilizer
                || recipe instanceof ClocheRecipe || recipe instanceof CokeOvenRecipe || recipe instanceof CrusherRecipe
                || recipe instanceof FermenterRecipe || recipe instanceof MetalPressRecipe || recipe instanceof MixerRecipe
                || recipe instanceof RefineryRecipe || recipe instanceof SawmillRecipe || recipe instanceof SqueezerRecipe;
    }

    @Override public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (!canImport(holder)) return null;
        var data = new IERecipeData();
        data.setInputs(new ArrayList<>()).setOutputs(new ArrayList<>()).setFluidInputs(new ArrayList<>());
        ResourceLocation type;
        switch (holder.value()) {
            case AlloyRecipe recipe -> {
                type = id("alloy");
                in(data, recipe.input0); in(data, recipe.input1); out(data, recipe.output); data.setTime(recipe.time);
            }
            case ArcRecyclingRecipe recipe -> {
                type = id("arc_furnace");
                in(data, recipe.input); data.setRecycling(true).setTime(recipe.getBaseTime()).setEnergy(recipe.getBaseEnergy());
                for (var output : recipe.getOutputs()) {
                    out(data, output.getFirst()); data.getRecyclingAmounts().add(output.getSecond());
                }
            }
            case ArcFurnaceRecipe recipe -> {
                type = id("arc_furnace");
                in(data, recipe.input);
                for (var additive : recipe.additives) in(data, additive);
                var main = recipe.output.getLazyList();
                if (main.size() > 6 || recipe.secondaryOutputs.size() > 4)
                    throw new RecipeImportException("viscript_recipe.editor.immersive_engineering.too_many_slots");
                for (int i = 0; i < Math.min(4, main.size()); i++) out(data, main.get(i));
                while (data.getOutputs().size() < 4) data.getOutputs().add(RecipeOutputData.empty());
                out(data, recipe.slag);
                for (var output : recipe.secondaryOutputs) chance(data, output);
                if (main.size() > 4) {
                    while (data.getOutputs().size() < 9) data.getOutputs().add(RecipeOutputData.empty());
                    for (int i = 4; i < main.size(); i++) out(data, main.get(i));
                }
                data.setArcSpecialType(recipe.specialRecipeType).setTime(recipe.getBaseTime()).setEnergy(recipe.getBaseEnergy());
            }
            case BlastFurnaceFuel recipe -> {
                type = id("blast_furnace_fuel"); in(data, recipe.input); data.setTime(recipe.burnTime);
            }
            case BlastFurnaceRecipe recipe -> {
                type = id("blast_furnace"); in(data, recipe.input); out(data, recipe.output); out(data, recipe.slag); data.setTime(recipe.time);
            }
            case BlueprintCraftingRecipe recipe -> {
                type = id("blueprint");
                for (var input : recipe.inputs) in(data, input);
                out(data, recipe.output); data.setBlueprintCategory(recipe.blueprintCategory);
            }
            case BottlingMachineRecipe recipe -> {
                type = id("bottling_machine");
                for (var input : recipe.inputs) in(data, input);
                for (var output : recipe.output.getLazyList()) out(data, output);
                addFluid(data, recipe.fluidInput.ingredient(), recipe.fluidInput.amount());
            }
            case ClocheFertilizer recipe -> {
                type = id("fertilizer"); in(data, recipe.input); data.setGrowthModifier(recipe.growthModifier);
            }
            case ClocheRecipe recipe -> {
                type = id("cloche"); in(data, recipe.seed); in(data, recipe.soil);
                for (var output : recipe.outputs) chance(data, output);
                data.setTime(recipe.time);
                addFluid(data, recipe.requiredFluid, 1000);
                data.setClocheRender(ClocheRenderFunction.CODECS.codec().encodeStart(JsonOps.INSTANCE, recipe.renderFunction)
                        .getOrThrow(error -> new IllegalArgumentException("Cannot encode cloche render function: " + error)).toString());
            }
            case CokeOvenRecipe recipe -> {
                type = id("coke_oven"); in(data, recipe.input); out(data, recipe.output);
                data.setTime(recipe.time).setCreosote(recipe.creosoteOutput);
            }
            case CrusherRecipe recipe -> {
                type = id("crusher"); in(data, recipe.input); out(data, recipe.output);
                for (var output : recipe.secondaryOutputs) chance(data, output);
                data.setEnergy(recipe.getBaseEnergy());
            }
            case FermenterRecipe recipe -> {
                type = id("fermenter"); in(data, recipe.input); out(data, recipe.itemOutput);
                data.setFluidOutput(recipe.fluidOutput.copy()).setEnergy(recipe.getBaseEnergy());
            }
            case MetalPressRecipe recipe -> {
                type = id("metal_press"); in(data, recipe.input); in(data, Ingredient.of(recipe.mold));
                out(data, recipe.output); data.setEnergy(recipe.getBaseEnergy());
            }
            case MixerRecipe recipe -> {
                type = id("mixer");
                for (var input : recipe.itemInputs) in(data, input);
                addFluid(data, recipe.fluidInput.ingredient(), recipe.fluidInput.amount());
                data.setFluidOutput(recipe.fluidOutput.copy()).setEnergy(recipe.getBaseEnergy());
            }
            case RefineryRecipe recipe -> {
                type = id("refinery");
                inOptional(data, recipe.catalyst);
                addFluid(data, recipe.input0.ingredient(), recipe.input0.amount());
                if (recipe.input1 != null) addFluid(data, recipe.input1.ingredient(), recipe.input1.amount());
                data.setFluidOutput(recipe.output.copy()).setEnergy(recipe.getBaseEnergy());
            }
            case SawmillRecipe recipe -> {
                type = id("sawmill"); in(data, recipe.input); out(data, recipe.output); out(data, recipe.stripped);
                for (var output : recipe.secondaryStripping.getLazyList()) out(data, output);
                while (data.getOutputs().size() < 6) data.getOutputs().add(RecipeOutputData.empty());
                for (var output : recipe.secondaryOutputs.getLazyList()) out(data, output);
                data.setEnergy(recipe.getBaseEnergy());
            }
            case SqueezerRecipe recipe -> {
                type = id("squeezer"); in(data, recipe.input); out(data, recipe.itemOutput);
                data.setFluidOutput(recipe.fluidOutput.copy()).setEnergy(recipe.getBaseEnergy());
            }
            default -> { return null; }
        }
        int maxInputs = switch (type.getPath()) {
            case "alloy", "cloche", "metal_press" -> 2;
            case "arc_furnace" -> data.isRecycling() ? 1 : 5;
            case "blueprint", "mixer" -> 6;
            case "bottling_machine" -> 4;
            default -> 1;
        };
        int maxOutputs = switch (type.getPath()) {
            case "arc_furnace" -> data.isRecycling() ? 4 : 11;
            case "bottling_machine", "cloche" -> 4;
            case "blast_furnace" -> 2;
            case "crusher" -> 5;
            case "sawmill" -> 10;
            case "fertilizer", "blast_furnace_fuel", "mixer", "refinery" -> 0;
            default -> 1;
        };
        if (data.getInputs().size() > maxInputs || data.getOutputs().size() > maxOutputs)
            throw new RecipeImportException("viscript_recipe.editor.immersive_engineering.too_many_slots");
        data.setCurrentEditorType(type);
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), type).setData(data));
    }

    private static ResourceLocation id(String path) { return IERecipeEditorTypes.id(path); }

    private static void in(IERecipeData data, IngredientWithSize value) throws RecipeImportException {
        data.getInputs().add(RecipeImporter.importIngredient(value.getBaseIngredient()).setCount(value.getCount()));
    }

    private static void in(IERecipeData data, Ingredient value) throws RecipeImportException {
        data.getInputs().add(RecipeImporter.importIngredient(value));
    }

    private static void inOptional(IERecipeData data, Ingredient value) throws RecipeImportException {
        data.getInputs().add(value.isEmpty() ? RecipeIngredient.empty() : RecipeImporter.importIngredient(value));
    }

    private static void out(IERecipeData data, TagOutput value) {
        data.getOutputs().add(RecipeOutputData.of(value.get().copy()));
    }

    private static void chance(IERecipeData data, StackWithChance value) {
        data.getOutputs().add(RecipeOutputData.of(value.stack().get().copy(), value.chance()));
    }

    private static void addFluid(IERecipeData data, FluidIngredient ingredient, int amount) {
        data.getFluidInputs().add(fluid(ingredient, amount));
        data.getFluidInputCodecs().add(FluidIngredient.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, com.lowdragmc.lowdraglib2.Platform.getFrozenRegistry()), ingredient)
                .getOrThrow(error -> new IllegalArgumentException("Cannot encode IE fluid ingredient: " + error)).toString());
    }

    private static FluidIngredientData fluid(FluidIngredient ingredient, int amount) {
        if (ingredient instanceof TagFluidIngredient tag) return FluidIngredientData.tag(tag.tag().location()).setAmount(amount);
        if (ingredient instanceof SingleFluidIngredient single)
            return FluidIngredientData.fluid(new FluidStack(single.fluid().value(), amount));
        if (ingredient.isEmpty()) return FluidIngredientData.empty();
        var stacks = ingredient.getStacks();
        return stacks.length == 0 ? FluidIngredientData.empty()
                : FluidIngredientData.fluid(stacks[0].copyWithAmount(amount));
    }
}
