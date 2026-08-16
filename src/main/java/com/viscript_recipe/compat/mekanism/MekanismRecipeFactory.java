package com.viscript_recipe.compat.mekanism;

import com.viscript_recipe.compat.mekanism.data.*;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.FluidIngredientKind;
import com.viscript_recipe.data.RecipeIngredient;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.basic.*;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.creator.IngredientCreatorAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Compiles editor data into Mekanism's native basic recipe implementations.
 */
public final class MekanismRecipeFactory {
    private MekanismRecipeFactory() {
    }

    /**
     * Compiles a recipe using the serializer represented by {@code type}.
     *
     * @param  type the Mekanism recipe serializer identifier
     * @param  data the editable recipe data
     * @return the native Mekanism recipe
     * @throws IllegalArgumentException if required input, output, amount, or registry data is invalid
     */
    @SuppressWarnings("all")
    public static Recipe<?> compile(ResourceLocation type, MekanismRecipeData data) {
        var kind = MekanismRecipeKind.byType(type)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported Mekanism recipe type: " + type));
        var itemInput = kind.itemInputs() > 0 ? itemIngredient(data.getItemInput(), data.getItemInput().getCount(), "item input") : null;
        var extraItemInput = kind.itemInputs() > 1 ? itemIngredient(data.getExtraItemInput(), data.getExtraItemInput().getCount(), "extra item input") : null;
        var fluidInput = kind.fluidInputs() > 0 ? fluidIngredient(data.getFluidInput()) : null;
        var chemicalInput = kind.chemicalInputs() > 0 ? chemicalIngredient(data.getChemicalInput(), "chemical input") : null;
        var extraChemicalInput = kind.chemicalInputs() > 1 ? chemicalIngredient(data.getExtraChemicalInput(), "extra chemical input") : null;

        return switch (kind) {
            case CRUSHING -> new BasicCrushingRecipe(itemInput, itemOutput(data));
            case ENRICHING -> new BasicEnrichingRecipe(itemInput, itemOutput(data));
            case SMELTING -> new BasicSmeltingRecipe(itemInput, itemOutput(data));
            case CHEMICAL_INFUSING -> new BasicChemicalInfuserRecipe(chemicalInput, extraChemicalInput, chemicalOutput(data.getChemicalOutput(), false));
            case COMBINING -> new BasicCombinerRecipe(itemInput, extraItemInput, itemOutput(data));
            case SEPARATING -> new BasicElectrolysisRecipe(fluidInput, positive(data.getEnergyMultiplier(), "energy multiplier"),
                    chemicalOutput(data.getChemicalOutput(), false), chemicalOutput(data.getSecondaryChemicalOutput(), false));
            case WASHING -> new BasicWashingRecipe(fluidInput, chemicalInput, chemicalOutput(data.getChemicalOutput(), false));
            case EVAPORATING -> new BasicFluidToFluidRecipe(fluidInput, fluidOutput(data));
            case ACTIVATING -> new BasicActivatingRecipe(chemicalInput, chemicalOutput(data.getChemicalOutput(), false));
            case CENTRIFUGING -> new BasicCentrifugingRecipe(chemicalInput, chemicalOutput(data.getChemicalOutput(), false));
            case CRYSTALLIZING -> new BasicChemicalCrystallizerRecipe(chemicalInput, itemOutput(data));
            case DISSOLUTION -> new BasicChemicalDissolutionRecipe(itemInput, chemicalInput, chemicalOutput(data.getChemicalOutput(), false), data.isPerTickUsage());
            case COMPRESSING -> new BasicCompressingRecipe(itemInput, chemicalInput, itemOutput(data), data.isPerTickUsage());
            case PURIFYING -> new BasicPurifyingRecipe(itemInput, chemicalInput, itemOutput(data), data.isPerTickUsage());
            case INJECTING -> new BasicInjectingRecipe(itemInput, chemicalInput, itemOutput(data), data.isPerTickUsage());
            case NUCLEOSYNTHESIZING -> new BasicNucleosynthesizingRecipe(itemInput, chemicalInput, itemOutput(data),
                    positive(data.getDuration(), "duration"), data.isPerTickUsage());
            case ENERGY_CONVERSION -> new BasicItemStackToEnergyRecipe(itemInput, positive(data.getEnergyOutput(), "energy output"));
            case CHEMICAL_CONVERSION -> new BasicChemicalConversionRecipe(itemInput, chemicalOutput(data.getChemicalOutput(), false));
            case OXIDIZING -> new BasicChemicalOxidizerRecipe(itemInput, chemicalOutput(data.getChemicalOutput(), false));
            case PIGMENT_EXTRACTING -> new BasicPigmentExtractingRecipe(itemInput, chemicalOutput(data.getChemicalOutput(), false));
            case PIGMENT_MIXING -> new BasicPigmentMixingRecipe(chemicalInput, extraChemicalInput, chemicalOutput(data.getChemicalOutput(), false));
            case METALLURGIC_INFUSING -> new BasicMetallurgicInfuserRecipe(itemInput, chemicalInput, itemOutput(data), data.isPerTickUsage());
            case PAINTING -> new BasicPaintingRecipe(itemInput, chemicalInput, itemOutput(data), data.isPerTickUsage());
            case REACTION -> new BasicPressurizedReactionRecipe(itemInput, fluidInput, chemicalInput,
                    nonNegative(data.getEnergyRequired(), "energy required"), positive(data.getDuration(), "duration"),
                    optionalItemOutput(data.getItemOutput()), chemicalOutput(data.getChemicalOutput(), true));
            case CONDENSENTRATING -> new BasicRotaryRecipe(chemicalInput, fluidOutput(data));
            case DECONDENSENTRATING -> new BasicRotaryRecipe(fluidInput,
                    chemicalOutput(data.getChemicalOutput(), false));
            case SAWING -> new BasicSawmillRecipe(itemInput, optionalItemOutput(data.getItemOutput()),
                    optionalItemOutput(data.getSecondaryItemOutput()), sawmillChance(data));
        };
    }

    private static ItemStackIngredient itemIngredient(RecipeIngredient data, int amount, String field) {
        var ingredient = data == null ? net.minecraft.world.item.crafting.Ingredient.EMPTY : data.compile();
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException("Mekanism " + field + " cannot be empty");
        }
        return IngredientCreatorAccess.item().from(ingredient, positive(amount, field + " amount"));
    }

    private static FluidStackIngredient fluidIngredient(FluidIngredientData data) {
        if (data == null) {
            throw new IllegalArgumentException("Mekanism fluid input cannot be empty");
        }
        var amount = positive(data.getKind() == FluidIngredientKind.FLUID && data.getFluid() != null
                ? data.getFluid().getAmount() : data.getAmount(), "fluid input amount");
        if (data.getKind() == FluidIngredientKind.TAG) {
            var id = requireId(data.getTag(), "fluid tag");
            return IngredientCreatorAccess.fluid().from(TagKey.create(Registries.FLUID, id), amount);
        }
        var stack = data.getFluid();
        if (stack == null || stack.isEmpty() || stack.getFluid() == Fluids.EMPTY) {
            throw new IllegalArgumentException("Mekanism fluid input cannot be empty");
        }
        return IngredientCreatorAccess.fluid().from(stack.copyWithAmount(amount));
    }

    private static ChemicalStackIngredient chemicalIngredient(MekanismChemicalIngredientData data, String field) {
        if (data == null) {
            throw new IllegalArgumentException("Mekanism " + field + " cannot be empty");
        }
        var amount = positive(data.getAmount(), field + " amount");
        if (data.getKind() == MekanismChemicalIngredientKind.TAG) {
            return IngredientCreatorAccess.chemicalStack().from(
                    TagKey.create(MekanismAPI.CHEMICAL_REGISTRY_NAME, requireId(data.getTag(), field + " tag")), amount);
        }
        return IngredientCreatorAccess.chemicalStack().fromHolder(chemicalHolder(data.getChemical(), field), amount);
    }

    private static ChemicalStack chemicalOutput(MekanismChemicalStackData data, boolean allowEmpty) {
        if (data == null || data.isEmpty()) {
            if (allowEmpty) {
                return ChemicalStack.EMPTY;
            }
            throw new IllegalArgumentException("Mekanism chemical output cannot be empty");
        }
        return new ChemicalStack(chemicalHolder(data.getChemical(), "chemical output"), positive(data.getAmount(), "chemical output amount"));
    }

    private static net.minecraft.core.Holder<Chemical> chemicalHolder(ResourceLocation id, String field) {
        var location = requireId(id, field);
        return MekanismAPI.CHEMICAL_REGISTRY.getHolder(location)
                .orElseThrow(() -> new IllegalArgumentException("Unknown Mekanism chemical for " + field + ": " + location));
    }

    private static ItemStack itemOutput(MekanismRecipeData data) {
        var stack = optionalItemOutput(data.getItemOutput());
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Mekanism item output cannot be empty");
        }
        return stack;
    }

    private static ItemStack optionalItemOutput(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack.copy();
    }

    private static FluidStack fluidOutput(MekanismRecipeData data) {
        var stack = data.getFluidOutput();
        if (stack == null || stack.isEmpty() || stack.getFluid() == Fluids.EMPTY) {
            throw new IllegalArgumentException("Mekanism fluid output cannot be empty");
        }
        return stack.copy();
    }

    private static double sawmillChance(MekanismRecipeData data) {
        var secondary = optionalItemOutput(data.getSecondaryItemOutput());
        if (secondary.isEmpty()) {
            if (optionalItemOutput(data.getItemOutput()).isEmpty()) {
                throw new IllegalArgumentException("Mekanism sawmill requires at least one output");
            }
            return 0;
        }
        var chance = data.getSecondaryChance();
        if (chance <= 0 || chance > 1) {
            throw new IllegalArgumentException("Mekanism sawmill secondary chance must be greater than 0 and at most 1");
        }
        return chance;
    }

    private static ResourceLocation requireId(ResourceLocation id, String field) {
        if (id == null) {
            throw new IllegalArgumentException("Mekanism " + field + " cannot be empty");
        }
        return id;
    }

    private static int positive(int value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException("Mekanism " + field + " must be positive");
        }
        return value;
    }

    private static long positive(long value, String field) {
        if (value <= 0) {
            throw new IllegalArgumentException("Mekanism " + field + " must be positive");
        }
        return value;
    }

    private static long nonNegative(long value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException("Mekanism " + field + " must not be negative");
        }
        return value;
    }
}
