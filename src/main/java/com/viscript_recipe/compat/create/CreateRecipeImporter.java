package com.viscript_recipe.compat.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.create.CreateFluidIngredientData;
import com.viscript_recipe.data.create.CreateFluidIngredientKind;
import com.viscript_recipe.data.create.CreateHeatCondition;
import com.viscript_recipe.data.create.CreateProcessingKind;
import com.viscript_recipe.data.create.CreateProcessingOutputData;
import com.viscript_recipe.data.create.CreateProcessingRecipeData;
import com.viscript_recipe.data.create.CreateSequencedAssemblyRecipeData;
import com.viscript_recipe.data.create.CreateSequencedAssemblyStepData;
import com.viscript_recipe.data.create.CreateSequencedAssemblyStepKind;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SingleFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.TagFluidIngredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class CreateRecipeImporter implements RecipeImportHandler {
    public static final CreateRecipeImporter INSTANCE = new CreateRecipeImporter();

    private CreateRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        if (holder == null || holder.value() == null) {
            return false;
        }
        var recipe = holder.value();
        if (recipe instanceof MechanicalCraftingRecipe || recipe instanceof SequencedAssemblyRecipe) {
            return true;
        }
        if (recipe instanceof ProcessingRecipe<?, ?> processing) {
            if (processing.getType() == AllRecipeTypes.DEPLOYING.getType()
                    || processing.getType() == AllRecipeTypes.ITEM_APPLICATION.getType()) {
                return true;
            }
            return CreateProcessingKind.byType(processing.getTypeInfo().getId()).isPresent();
        }
        return false;
    }

    @Nullable
    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        var recipe = holder.value();
        if (recipe instanceof MechanicalCraftingRecipe mechanicalCrafting) {
            return success(RecipeImporter.importMechanicalCrafting(holder.id(), mechanicalCrafting, mechanicalCrafting.acceptsMirrored(), provider));
        }
        if (recipe instanceof SequencedAssemblyRecipe sequencedAssembly) {
            return success(importSequencedAssembly(holder.id(), sequencedAssembly));
        }
        if (recipe instanceof ProcessingRecipe<?, ?> processing) {
            return success(importProcessing(holder.id(), processing));
        }
        return null;
    }

    private static RecipeImportResult success(RecipeEntry entry) {
        return RecipeImporter.success(entry);
    }

    private static RecipeEntry importProcessing(ResourceLocation id, ProcessingRecipe<?, ?> recipe) throws RecipeImportException {
        var kind = kindFor(recipe);
        var data = new CreateProcessingRecipeData()
                .setIngredients(importIngredients(recipe.getIngredients(), kind.maxItemInputs()))
                .setFluidIngredients(importFluidIngredients(recipe.getFluidIngredients(), kind.maxFluidInputs()))
                .setOutputs(importOutputs(recipe.getRollableResults(), kind.maxItemOutputs()))
                .setFluidOutputs(importFluidOutputs(recipe.getFluidResults(), kind.maxFluidOutputs()))
                .setProcessingTime(Math.max(0, recipe.getProcessingDuration()))
                .setHeatRequirement(importHeat(recipe.getRequiredHeat()));
        if (recipe instanceof ItemApplicationRecipe itemApplication) {
            data.setKeepHeldItem(itemApplication.shouldKeepHeldItem());
        }
        return RecipeImporter.baseEntry(id, kind.typeId()).setCreateProcessing(data);
    }

    private static CreateProcessingKind kindFor(ProcessingRecipe<?, ?> recipe) throws RecipeImportException {
        if (recipe.getType() == AllRecipeTypes.DEPLOYING.getType()) {
            return CreateProcessingKind.DEPLOYING;
        }
        if (recipe.getType() == AllRecipeTypes.ITEM_APPLICATION.getType()) {
            return CreateProcessingKind.ITEM_APPLICATION;
        }
        var typeId = recipe.getTypeInfo().getId();
        return CreateProcessingKind.byType(typeId)
                .orElseThrow(() -> new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_type", typeId.toString()));
    }

    private static RecipeEntry importSequencedAssembly(ResourceLocation id, SequencedAssemblyRecipe recipe) throws RecipeImportException {
        var data = new CreateSequencedAssemblyRecipeData()
                .setIngredient(RecipeImporter.importIngredient(recipe.getIngredient()))
                .setTransitionalItem(recipe.getTransitionalItem().copyWithCount(1))
                .setOutputs(importOutputs(recipe.resultPool, 9))
                .setLoops(Math.max(1, recipe.getLoops()));
        var steps = new ArrayList<CreateSequencedAssemblyStepData>();
        for (var sequencedRecipe : recipe.getSequence()) {
            var stepRecipe = sequencedRecipe.getRecipe();
            steps.add(importSequencedStep(stepRecipe));
        }
        if (steps.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_sequence");
        }
        data.setSequence(steps);
        return RecipeImporter.baseEntry(id, com.viscript_recipe.data.RecipeEditorTypes.CREATE_SEQUENCED_ASSEMBLY)
                .setCreateSequencedAssembly(data);
    }

    private static CreateSequencedAssemblyStepData importSequencedStep(ProcessingRecipe<?, ?> recipe) throws RecipeImportException {
        var data = new CreateSequencedAssemblyStepData();
        if (recipe instanceof ItemApplicationRecipe itemApplication) {
            data.setKind(CreateSequencedAssemblyStepKind.DEPLOYING)
                    .setIngredient(RecipeImporter.importIngredient(itemApplication.getRequiredHeldItem()))
                    .setKeepHeldItem(itemApplication.shouldKeepHeldItem());
            return data;
        }
        if (recipe instanceof PressingRecipe) {
            data.setKind(CreateSequencedAssemblyStepKind.PRESSING);
            return data;
        }
        if (recipe instanceof CuttingRecipe) {
            data.setKind(CreateSequencedAssemblyStepKind.CUTTING)
                    .setProcessingTime(Math.max(0, recipe.getProcessingDuration()));
            return data;
        }
        if (recipe instanceof FillingRecipe filling) {
            data.setKind(CreateSequencedAssemblyStepKind.FILLING)
                    .setFluidIngredient(importFluidIngredient(filling.getRequiredFluid()));
            return data;
        }
        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_type", recipe.getTypeInfo().getId().toString());
    }

    private static List<com.viscript_recipe.data.RecipeIngredient> importIngredients(List<Ingredient> ingredients, int max) throws RecipeImportException {
        if (ingredients.size() > max) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_ingredients", ingredients.size(), max);
        }
        var imported = new ArrayList<com.viscript_recipe.data.RecipeIngredient>();
        for (var ingredient : ingredients) {
            if (ingredient != null && !ingredient.isEmpty()) {
                imported.add(RecipeImporter.importIngredient(ingredient));
            }
        }
        return imported;
    }

    private static List<CreateFluidIngredientData> importFluidIngredients(List<SizedFluidIngredient> ingredients, int max) throws RecipeImportException {
        if (ingredients.size() > max) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_fluid_ingredients", ingredients.size(), max);
        }
        var imported = new ArrayList<CreateFluidIngredientData>();
        for (var ingredient : ingredients) {
            imported.add(importFluidIngredient(ingredient));
        }
        return imported;
    }

    private static CreateFluidIngredientData importFluidIngredient(SizedFluidIngredient ingredient) throws RecipeImportException {
        if (ingredient == null || ingredient.ingredient().isEmpty() || ingredient.ingredient().hasNoFluids()) {
            return CreateFluidIngredientData.empty();
        }
        var fluidIngredient = ingredient.ingredient();
        if (fluidIngredient instanceof TagFluidIngredient tag) {
            return new CreateFluidIngredientData()
                    .setKind(CreateFluidIngredientKind.TAG)
                    .setTag(tag.tag().location())
                    .setAmount(Math.max(1, ingredient.amount()));
        }
        if (fluidIngredient instanceof SingleFluidIngredient single) {
            return CreateFluidIngredientData.fluid(new FluidStack(single.fluid(), Math.max(1, ingredient.amount())));
        }
        var stacks = ingredient.getFluids();
        if (fluidIngredient.isSimple() && stacks.length == 1) {
            return CreateFluidIngredientData.fluid(stacks[0].copyWithAmount(Math.max(1, ingredient.amount())));
        }
        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_fluid_ingredient");
    }

    private static List<CreateProcessingOutputData> importOutputs(List<ProcessingOutput> outputs, int max) throws RecipeImportException {
        if (outputs.size() > max) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_outputs", outputs.size(), max);
        }
        var imported = new ArrayList<CreateProcessingOutputData>();
        for (var output : outputs) {
            if (output == null) {
                continue;
            }
            var stack = output.getStack();
            if (!stack.isEmpty()) {
                imported.add(new CreateProcessingOutputData()
                        .setItem(stack.copy())
                        .setChance(output.getChance()));
            }
        }
        return imported;
    }

    private static List<FluidStack> importFluidOutputs(List<FluidStack> outputs, int max) throws RecipeImportException {
        if (outputs.size() > max) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.too_many_fluid_outputs", outputs.size(), max);
        }
        var imported = new ArrayList<FluidStack>();
        for (var output : outputs) {
            if (output != null && !output.isEmpty() && output.getFluid() != Fluids.EMPTY) {
                imported.add(output.copy());
            }
        }
        return imported;
    }

    private static CreateHeatCondition importHeat(HeatCondition condition) {
        return switch (condition == null ? HeatCondition.NONE : condition) {
            case NONE -> CreateHeatCondition.NONE;
            case HEATED -> CreateHeatCondition.HEATED;
            case SUPERHEATED -> CreateHeatCondition.SUPERHEATED;
        };
    }
}
