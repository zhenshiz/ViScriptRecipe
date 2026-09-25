package com.viscript_recipe.compat.immersive_engineering;

import blusunrize.immersiveengineering.api.crafting.*;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.compat.immersive_engineering.data.IERecipeData;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.FluidIngredientKind;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.RecipeOutputData;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

/** Compiles editable IE data into the same native recipe classes registered by IE. */
public final class IERecipeFactory {
    private IERecipeFactory() {}

    public static Recipe<?> compile(ResourceLocation type, IERecipeData data) {
        var kind = type.getPath();
        var first = input(data, 0, !kind.equals("refinery"));
        var result = output(data, 0, !(kind.equals("fertilizer") || kind.equals("blast_furnace_fuel")
                || kind.equals("mixer") || kind.equals("refinery") || kind.equals("fermenter")
                || kind.equals("squeezer")));
        var energy = positive(data.getEnergy(), "Energy");
        var time = positive(data.getTime(), "Time");
        return switch (kind) {
            case "alloy" -> new AlloyRecipe(tag(result), sized(first), sized(input(data, 1, true)), time);
            case "arc_furnace" -> arcFurnace(data, first, time, energy);
            case "blast_furnace_fuel" -> new BlastFurnaceFuel(first.compile(), time);
            case "blast_furnace" -> new BlastFurnaceRecipe(tag(result), sized(first), time, tag(output(data, 1, false)));
            case "blueprint" -> new BlueprintCraftingRecipe(nonblank(data.getBlueprintCategory(), "Blueprint category"), tag(result), sizedInputs(data, 6));
            case "bottling_machine" -> new BottlingMachineRecipe(tagOutputs(data, 4, true), sizedInputs(data, 4), sizedFluid(data, 0));
            case "fertilizer" -> new ClocheFertilizer(first.compile(), positiveFloat(data.getGrowthModifier(), "Growth modifier"));
            case "cloche" -> new ClocheRecipe(chanceOutputs(data, 4, true), first.compile(), input(data, 1, true).compile(), time,
                    nativeFluid(data, 0),
                    ClocheRenderFunction.CODECS.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(data.getClocheRender()))
                            .getOrThrow(error -> new IllegalArgumentException("Invalid cloche render function: " + error)));
            case "coke_oven" -> new CokeOvenRecipe(tag(result), sized(first), time, Math.max(0, data.getCreosote()));
            case "crusher" -> new CrusherRecipe(tag(result), first.compile(), energy, chanceOutputs(data, 5, false, 1));
            case "fermenter" -> new FermenterRecipe(fluidOutput(data), tag(output(data, 0, false)), sized(first), energy);
            case "metal_press" -> new MetalPressRecipe(tag(result), sized(first), mold(input(data, 1, true)), energy);
            case "mixer" -> new MixerRecipe(fluidOutput(data), sizedFluid(data, 0), sizedInputs(data, 6), energy);
            case "refinery" -> new RefineryRecipe(fluidOutput(data), sizedFluid(data, 0),
                    data.getFluidInputs().size() > 1 && !data.getFluidInputs().get(1).isEmpty() ? sizedFluid(data, 1) : null,
                    first.isEmpty() ? Ingredient.EMPTY : first.compile(), energy);
            case "sawmill" -> new SawmillRecipe(tag(result), tag(output(data, 1, false)), first.compile(), energy,
                    tagOutputsRange(data, 2, 6), tagOutputsRange(data, 6, 10));
            case "squeezer" -> new SqueezerRecipe(fluidOutput(data), tag(output(data, 0, false)), sized(first), energy);
            default -> throw new IllegalArgumentException("Unsupported IE recipe type: " + type);
        };
    }

    private static ArcFurnaceRecipe arcFurnace(IERecipeData data, RecipeIngredient input, int time, int energy) {
        if (data.isRecycling()) {
            var results = new ArrayList<Pair<TagOutput, Double>>();
            for (int i = 0; i < Math.min(4, data.getOutputs().size()); i++) {
                var out = output(data, i, false);
                if (!out.isEmpty()) {
                    var amount = i < data.getRecyclingAmounts().size() ? data.getRecyclingAmounts().get(i) : (double) out.getItem().getCount();
                    results.add(Pair.of(tag(out), positiveDouble(amount, "Recycling amount")));
                }
            }
            if (results.isEmpty()) throw new IllegalArgumentException("Recycling requires at least one output");
            return new ArcRecyclingRecipe(() -> (RegistryAccess) com.lowdragmc.lowdraglib2.Platform.getFrozenRegistry(),
                    results, sized(input), time, energy);
        }
        var main = new ArrayList<TagOutput>(tagOutputs(data, 4, true).getLazyList());
        main.addAll(tagOutputsRange(data, 9, 11).getLazyList());
        return new ArcFurnaceRecipe(new TagOutputList(main), tag(output(data, 4, false)),
                chanceOutputs(data, 9, false, 5), time, energy, sized(input), sizedInputs(data, 4, 1), data.getArcSpecialType());
    }

    private static RecipeIngredient input(IERecipeData data, int index, boolean required) {
        var value = index < data.getInputs().size() ? data.getInputs().get(index) : RecipeIngredient.empty();
        if (value == null) value = RecipeIngredient.empty();
        if (required && value.isEmpty()) throw new IllegalArgumentException("IE item input " + (index + 1) + " is empty");
        return value;
    }

    private static RecipeOutputData output(IERecipeData data, int index, boolean required) {
        var value = index < data.getOutputs().size() ? data.getOutputs().get(index) : RecipeOutputData.empty();
        if (value == null) value = RecipeOutputData.empty();
        if (required && value.isEmpty()) throw new IllegalArgumentException("IE item output " + (index + 1) + " is empty");
        return value;
    }

    private static IngredientWithSize sized(RecipeIngredient data) {
        return new IngredientWithSize(data.compile(), Math.max(1, data.getCount()));
    }

    private static List<IngredientWithSize> sizedInputs(IERecipeData data, int max) { return sizedInputs(data, max, 0); }

    private static List<IngredientWithSize> sizedInputs(IERecipeData data, int max, int start) {
        var result = new ArrayList<IngredientWithSize>();
        for (int i = start; i < start + max && i < data.getInputs().size(); i++) {
            var value = data.getInputs().get(i);
            if (value != null && !value.isEmpty()) result.add(sized(value));
        }
        return result;
    }

    private static TagOutput tag(RecipeOutputData data) { return data.isEmpty() ? TagOutput.EMPTY : new TagOutput(data.getItem().copy()); }

    private static TagOutputList tagOutputs(IERecipeData data, int max, boolean required) {
        return tagOutputsRange(data, 0, max, required);
    }

    private static TagOutputList tagOutputsRange(IERecipeData data, int start, int end) {
        return tagOutputsRange(data, start, end, false);
    }

    private static TagOutputList tagOutputsRange(IERecipeData data, int start, int end, boolean required) {
        var result = new ArrayList<TagOutput>();
        for (int i = start; i < end && i < data.getOutputs().size(); i++) {
            var value = output(data, i, false);
            if (!value.isEmpty()) result.add(tag(value));
        }
        if (required && result.isEmpty()) throw new IllegalArgumentException("IE recipe output is empty");
        return new TagOutputList(result);
    }

    private static List<StackWithChance> chanceOutputs(IERecipeData data, int max, boolean required) {
        return chanceOutputs(data, max, required, 0);
    }

    private static List<StackWithChance> chanceOutputs(IERecipeData data, int max, boolean required, int start) {
        var result = new ArrayList<StackWithChance>();
        for (int i = start; i < max && i < data.getOutputs().size(); i++) {
            var value = output(data, i, false);
            if (!value.isEmpty()) result.add(new StackWithChance(value.getItem().copy(), value.getChance()));
        }
        if (required && result.isEmpty()) throw new IllegalArgumentException("IE recipe output is empty");
        return result;
    }

    private static FluidIngredientData fluidInput(IERecipeData data, int index, boolean required) {
        var value = index < data.getFluidInputs().size() ? data.getFluidInputs().get(index) : FluidIngredientData.empty();
        if (value == null) value = FluidIngredientData.empty();
        if (required && value.isEmpty()) throw new IllegalArgumentException("IE fluid input " + (index + 1) + " is empty");
        return value;
    }

    private static SizedFluidIngredient sizedFluid(IERecipeData data, int index) {
        return new SizedFluidIngredient(nativeFluid(data, index), fluidInput(data, index, true).getAmount());
    }

    private static FluidIngredient nativeFluid(IERecipeData data, int index) {
        var value = fluidInput(data, index, true);
        if (index < data.getFluidInputCodecs().size()) {
            var raw = data.getFluidInputCodecs().get(index);
            if (raw != null && !raw.isBlank())
                return FluidIngredient.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, com.lowdragmc.lowdraglib2.Platform.getFrozenRegistry()), JsonParser.parseString(raw))
                        .getOrThrow(error -> new IllegalArgumentException("Invalid IE fluid ingredient: " + error));
        }
        if (value.getKind() == FluidIngredientKind.TAG)
            return FluidIngredient.tag(TagKey.create(Registries.FLUID, value.getTag()));
        var stack = value.getFluid();
        return stack.getComponents().isEmpty() ? FluidIngredient.single(stack)
                : DataComponentFluidIngredient.of(false, stack);
    }

    private static FluidStack fluidOutput(IERecipeData data) {
        return data.getFluidOutput() == null ? FluidStack.EMPTY : data.getFluidOutput().copy();
    }

    private static net.minecraft.world.item.Item mold(RecipeIngredient ingredient) {
        if (ingredient.getKind() != com.viscript_recipe.data.IngredientValueKind.ITEM || ingredient.toStack().isEmpty())
            throw new IllegalArgumentException("Metal press mold must be a single item");
        return ingredient.toStack().getItem();
    }

    private static int positive(int value, String name) {
        if (value <= 0) throw new IllegalArgumentException(name + " must be positive");
        return value;
    }

    private static float positiveFloat(float value, String name) {
        if (!Float.isFinite(value) || value <= 0) throw new IllegalArgumentException(name + " must be positive");
        return value;
    }

    private static double positiveDouble(double value, String name) {
        if (!Double.isFinite(value) || value <= 0) throw new IllegalArgumentException(name + " must be positive");
        return value;
    }

    private static String nonblank(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is empty");
        return value;
    }
}
