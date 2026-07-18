package com.viscript_recipe.compat.mekanism;

import com.viscript_recipe.data.mekanism.*;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.*;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.api.recipes.ingredients.chemical.SingleChemicalIngredient;
import mekanism.api.recipes.ingredients.chemical.TagChemicalIngredient;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.crafting.SingleFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.TagFluidIngredient;

import java.util.ArrayList;

/**
 * Imports native Mekanism recipe objects into the shared typed editor model.
 */
public final class MekanismRecipeImporter implements RecipeImportHandler {
    public static final MekanismRecipeImporter INSTANCE = new MekanismRecipeImporter();

    private MekanismRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && kind(holder) != null;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        var kind = kind(holder);
        if (kind == null) {
            return null;
        }
        var recipe = holder.value();
        if (recipe instanceof RotaryRecipe rotaryRecipe) {
            return importRotary(holder, rotaryRecipe);
        }
        var data = new MekanismRecipeData();

        switch (kind) {
            case CRUSHING, ENRICHING, SMELTING -> {
                var typed = (ItemStackToItemStackRecipe) recipe;
                importItemInput(data, typed.getInput(), false);
                data.setItemOutput(copyFirstItem(typed.getOutputDefinition()));
            }
            case CHEMICAL_INFUSING, PIGMENT_MIXING -> {
                var typed = (ChemicalChemicalToChemicalRecipe) recipe;
                data.setChemicalInput(importChemicalInput(typed.getLeftInput()))
                        .setExtraChemicalInput(importChemicalInput(typed.getRightInput()))
                        .setChemicalOutput(copyFirstChemical(typed.getOutputDefinition()));
            }
            case COMBINING -> {
                var typed = (CombinerRecipe) recipe;
                importItemInput(data, typed.getMainInput(), false);
                importItemInput(data, typed.getExtraInput(), true);
                data.setItemOutput(copyFirstItem(typed.getOutputDefinition()));
            }
            case SEPARATING -> {
                var typed = (ElectrolysisRecipe) recipe;
                var output = typed.getOutputDefinition().getFirst();
                data.setFluidInput(importFluidInput(typed.getInput()))
                        .setEnergyMultiplier(Math.max(1, typed.getEnergyMultiplier()))
                        .setChemicalOutput(copyChemical(output.left()))
                        .setSecondaryChemicalOutput(copyChemical(output.right()));
            }
            case WASHING -> {
                var typed = (FluidChemicalToChemicalRecipe) recipe;
                data.setFluidInput(importFluidInput(typed.getFluidInput()))
                        .setChemicalInput(importChemicalInput(typed.getChemicalInput()))
                        .setChemicalOutput(copyFirstChemical(typed.getOutputDefinition()));
            }
            case EVAPORATING -> {
                var typed = (FluidToFluidRecipe) recipe;
                data.setFluidInput(importFluidInput(typed.getInput()))
                        .setFluidOutput(typed.getOutputDefinition().getFirst().copy());
            }
            case ACTIVATING, CENTRIFUGING -> {
                var typed = (ChemicalToChemicalRecipe) recipe;
                data.setChemicalInput(importChemicalInput(typed.getInput()))
                        .setChemicalOutput(copyFirstChemical(typed.getOutputDefinition()));
            }
            case CRYSTALLIZING -> {
                var typed = (ChemicalCrystallizerRecipe) recipe;
                data.setChemicalInput(importChemicalInput(typed.getInput()))
                        .setItemOutput(copyFirstItem(typed.getOutputDefinition()));
            }
            case DISSOLUTION -> {
                var typed = (ChemicalDissolutionRecipe) recipe;
                importItemInput(data, typed.getItemInput(), false);
                data.setChemicalInput(importChemicalInput(typed.getChemicalInput()))
                        .setChemicalOutput(copyFirstChemical(typed.getOutputDefinition()))
                        .setPerTickUsage(typed.perTickUsage());
            }
            case COMPRESSING, PURIFYING, INJECTING, METALLURGIC_INFUSING, PAINTING -> {
                var typed = (ItemStackChemicalToItemStackRecipe) recipe;
                importItemInput(data, typed.getItemInput(), false);
                data.setChemicalInput(importChemicalInput(typed.getChemicalInput()))
                        .setItemOutput(copyFirstItem(typed.getOutputDefinition()))
                        .setPerTickUsage(typed.perTickUsage());
            }
            case NUCLEOSYNTHESIZING -> {
                var typed = (NucleosynthesizingRecipe) recipe;
                importItemInput(data, typed.getItemInput(), false);
                data.setChemicalInput(importChemicalInput(typed.getChemicalInput()))
                        .setItemOutput(copyFirstItem(typed.getOutputDefinition()))
                        .setDuration(Math.max(1, typed.getDuration()))
                        .setPerTickUsage(typed.perTickUsage());
            }
            case ENERGY_CONVERSION -> {
                var typed = (ItemStackToEnergyRecipe) recipe;
                importItemInput(data, typed.getInput(), false);
                data.setEnergyOutput(Math.max(1, typed.getOutputDefinition()[0]));
            }
            case CHEMICAL_CONVERSION, OXIDIZING, PIGMENT_EXTRACTING -> {
                var typed = (ItemStackToChemicalRecipe) recipe;
                importItemInput(data, typed.getInput(), false);
                data.setChemicalOutput(copyFirstChemical(typed.getOutputDefinition()));
            }
            case REACTION -> {
                var typed = (PressurizedReactionRecipe) recipe;
                var output = typed.getOutputDefinition().getFirst();
                importItemInput(data, typed.getInputSolid(), false);
                data.setFluidInput(importFluidInput(typed.getInputFluid()))
                        .setChemicalInput(importChemicalInput(typed.getInputChemical()))
                        .setEnergyRequired(Math.max(0, typed.getEnergyRequired()))
                        .setDuration(Math.max(1, typed.getDuration()))
                        .setItemOutput(output.item().copy())
                        .setChemicalOutput(copyChemical(output.chemical()));
            }
            case CONDENSENTRATING, DECONDENSENTRATING -> throw new IllegalStateException(
                    "Rotary recipes must be imported before the serializer switch"
            );
            case SAWING -> {
                var typed = (SawmillRecipe) recipe;
                importItemInput(data, typed.getInput(), false);
                data.setItemOutput(copyFirstItemOrEmpty(typed.getMainOutputDefinition()))
                        .setSecondaryItemOutput(copyFirstItemOrEmpty(typed.getSecondaryOutputDefinition()))
                        .setSecondaryChance(typed.getSecondaryChance());
            }
        }
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), kind.typeId()).setData(data));
    }

    private static RecipeImportResult importRotary(RecipeHolder<?> holder, RotaryRecipe recipe)
            throws RecipeImportException {
        var entries = new ArrayList<com.viscript_recipe.data.RecipeEntry>(2);
        boolean split = recipe.hasChemicalToFluid() && recipe.hasFluidToChemical();
        if (recipe.hasChemicalToFluid()) {
            var data = new MekanismRecipeData()
                    .setChemicalInput(importChemicalInput(recipe.getChemicalInput()))
                    .setFluidOutput(recipe.getFluidOutputDefinition().getFirst().copy());
            var id = split ? splitRotaryId(holder.id(), "condensentrating") : holder.id();
            entries.add(RecipeImporter.baseEntry(id, MekanismRecipeKind.CONDENSENTRATING.typeId())
                    .setData(data));
        }
        if (recipe.hasFluidToChemical()) {
            var data = new MekanismRecipeData()
                    .setFluidInput(importFluidInput(recipe.getFluidInput()))
                    .setChemicalOutput(copyFirstChemical(recipe.getChemicalOutputDefinition()));
            var id = split ? splitRotaryId(holder.id(), "decondensentrating") : holder.id();
            entries.add(RecipeImporter.baseEntry(id, MekanismRecipeKind.DECONDENSENTRATING.typeId())
                    .setData(data));
        }
        if (entries.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_result");
        }
        if (!split) {
            return RecipeImporter.success(entries.getFirst());
        }
        return RecipeImportResult.success(entries, Component.translatable(
                "viscript_recipe.editor.import_recipe.success.mekanism_rotary_split",
                holder.id().toString()
        ));
    }

    private static ResourceLocation splitRotaryId(ResourceLocation id, String direction) {
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + '_' + direction);
    }

    private static void importItemInput(MekanismRecipeData data, ItemStackIngredient input, boolean extra) throws RecipeImportException {
        var imported = RecipeImporter.importIngredient(input.ingredient().ingredient());
        var amount = Math.max(1, input.ingredient().count());
        imported = MekanismItemInputCounts.copyWithItemAmount(imported, amount);
        if (extra) {
            data.setExtraItemInput(imported).setExtraItemInputAmount(amount);
        } else {
            data.setItemInput(imported).setItemInputAmount(amount);
        }
    }

    private static MekanismFluidIngredientData importFluidInput(FluidStackIngredient input) throws RecipeImportException {
        var sized = input.ingredient();
        var amount = Math.max(1, sized.amount());
        if (sized.ingredient() instanceof TagFluidIngredient tag) {
            return new MekanismFluidIngredientData()
                    .setKind(MekanismFluidIngredientKind.TAG)
                    .setTag(tag.tag().location())
                    .setAmount(amount);
        }
        if (sized.ingredient() instanceof SingleFluidIngredient single) {
            return new MekanismFluidIngredientData()
                    .setKind(MekanismFluidIngredientKind.FLUID)
                    .setFluid(new net.neoforged.neoforge.fluids.FluidStack(single.fluid(), amount))
                    .setAmount(amount);
        }
        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.mekanism_unsupported_fluid_ingredient");
    }

    private static MekanismChemicalIngredientData importChemicalInput(ChemicalStackIngredient input) throws RecipeImportException {
        if (input.ingredient() instanceof SingleChemicalIngredient single) {
            return new MekanismChemicalIngredientData()
                    .setKind(MekanismChemicalIngredientKind.CHEMICAL)
                    .setChemical(holderId(single.chemical()))
                    .setAmount(Math.max(1, input.amount()));
        }
        if (input.ingredient() instanceof TagChemicalIngredient tag) {
            return new MekanismChemicalIngredientData()
                    .setKind(MekanismChemicalIngredientKind.TAG)
                    .setTag(tag.tag().location())
                    .setAmount(Math.max(1, input.amount()));
        }
        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.mekanism_unsupported_chemical_ingredient");
    }

    private static MekanismChemicalStackData copyFirstChemical(java.util.List<ChemicalStack> stacks) throws RecipeImportException {
        if (stacks == null || stacks.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_result");
        }
        return copyChemical(stacks.getFirst());
    }

    private static MekanismChemicalStackData copyChemical(ChemicalStack stack) {
        if (stack == null || stack.isEmpty()) {
            return new MekanismChemicalStackData().setChemical(null).setAmount(0);
        }
        return new MekanismChemicalStackData()
                .setChemical(holderId(stack.getChemicalHolder()))
                .setAmount(stack.getAmount());
    }

    private static ResourceLocation holderId(net.minecraft.core.Holder<?> holder) {
        return holder.unwrapKey().map(ResourceKey::location).orElseThrow();
    }

    private static ItemStack copyFirstItem(java.util.List<ItemStack> stacks) throws RecipeImportException {
        var result = copyFirstItemOrEmpty(stacks);
        if (result.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_result");
        }
        return result;
    }

    private static ItemStack copyFirstItemOrEmpty(java.util.List<ItemStack> stacks) {
        return stacks == null || stacks.isEmpty() ? ItemStack.EMPTY : stacks.getFirst().copy();
    }

    private static MekanismRecipeKind kind(RecipeHolder<?> holder) {
        if (holder.value() instanceof RotaryRecipe) {
            return MekanismRecipeKind.CONDENSENTRATING;
        }
        var serializer = BuiltInRegistries.RECIPE_SERIALIZER.getKey(holder.value().getSerializer());
        return MekanismRecipeKind.byType(serializer).orElse(null);
    }
}
