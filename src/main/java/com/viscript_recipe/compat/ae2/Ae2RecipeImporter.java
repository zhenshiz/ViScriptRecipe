package com.viscript_recipe.compat.ae2;

import appeng.recipes.entropy.EntropyMode;
import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.entropy.PropertyValueMatcher;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.transform.TransformRecipe;
import com.viscript_recipe.compat.ae2.data.*;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.importer.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Imports native AE2 recipes without flattening state constraints or optional press slots. */
public final class Ae2RecipeImporter implements RecipeImportHandler {
    public static final Ae2RecipeImporter INSTANCE = new Ae2RecipeImporter();
    private Ae2RecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && (holder.value() instanceof ChargerRecipe || holder.value() instanceof InscriberRecipe
                || holder.value() instanceof TransformRecipe || holder.value() instanceof EntropyRecipe);
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        IVSRecipeData data;
        ResourceLocation type;
        switch (holder.value()) {
            case ChargerRecipe recipe -> {
                type = Ae2RecipeEditorTypes.CHARGER;
                data = new Ae2ChargerRecipeData().setInput(importIngredient(recipe.getIngredient()))
                        .setResult(recipe.getResultItem().copy());
            }
            case InscriberRecipe recipe -> {
                type = Ae2RecipeEditorTypes.INSCRIBER;
                data = new Ae2InscriberRecipeData().setMiddle(importIngredient(recipe.getMiddleInput()))
                        .setTop(importIngredient(recipe.getTopOptional()))
                        .setBottom(importIngredient(recipe.getBottomOptional()))
                        .setResult(recipe.getResultItem().copy())
                        .setConsumePresses(recipe.getProcessType() == InscriberProcessType.PRESS);
            }
            case TransformRecipe recipe -> {
                type = Ae2RecipeEditorTypes.TRANSFORM;
                var inputs = new ArrayList<Ae2IngredientData>();
                for (var ingredient : recipe.getIngredients()) inputs.add(importIngredient(ingredient));
                var transform = new Ae2TransformRecipeData().setInputs(inputs).setResult(recipe.getResultItem().copy())
                        .setExplosion(recipe.circumstance.isExplosion());
                if (recipe.circumstance.isFluid()) {
                    transform.setFluidTag(ResourceLocation.parse(recipe.circumstance.toJson().get("tag").getAsString()));
                }
                data = transform;
            }
            case EntropyRecipe recipe -> {
                type = Ae2RecipeEditorTypes.ENTROPY;
                var entropy = new Ae2EntropyRecipeData().setHeat(recipe.getMode() == EntropyMode.HEAT)
                        .setInputBlockEnabled(recipe.getInput().block().isPresent())
                        .setInputFluidEnabled(recipe.getInput().fluid().isPresent())
                        .setOutputBlockEnabled(recipe.getOutput().block().isPresent())
                        .setOutputFluidEnabled(recipe.getOutput().fluid().isPresent());
                recipe.getInput().block().ifPresent(block -> entropy.setInputBlock(BuiltInRegistries.BLOCK.getKey(block.block()))
                        .setInputBlockProperties(importMatchers(block.properties())));
                recipe.getInput().fluid().ifPresent(fluid -> entropy.setInputFluid(BuiltInRegistries.FLUID.getKey(fluid.fluid()))
                        .setInputFluidProperties(importMatchers(fluid.properties())));
                recipe.getOutput().block().ifPresent(block -> entropy.setOutputBlock(BuiltInRegistries.BLOCK.getKey(block.block()))
                        .setKeepBlockProperties(block.keepProperties()).setOutputBlockProperties(importAppliers(block.properties())));
                recipe.getOutput().fluid().ifPresent(fluid -> entropy.setOutputFluid(BuiltInRegistries.FLUID.getKey(fluid.fluid()))
                        .setKeepFluidProperties(fluid.keepProperties()).setOutputFluidProperties(importAppliers(fluid.properties())));
                entropy.setDrops(new ArrayList<>(recipe.getDrops().stream().map(stack -> stack.copy()).toList()));
                data = entropy;
            }
            default -> { return null; }
        }
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), type).setData(data));
    }

    private static Ae2IngredientData importIngredient(Ingredient ingredient) throws RecipeImportException {
        var data = new Ae2IngredientData();
        if (ingredient.isEmpty()) return data;
        if (ingredient.isCustom()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_ingredient");
        }
        for (var value : ingredient.getValues()) {
            if (value instanceof Ingredient.ItemValue item) data.getAlternatives().add(RecipeIngredient.item(item.item().copy()));
            else if (value instanceof Ingredient.TagValue tag) data.getAlternatives().add(RecipeIngredient.tag(tag.tag().location()));
            else throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_ingredient");
        }
        return data;
    }

    private static List<Ae2StatePropertyData> importMatchers(Map<String, PropertyValueMatcher> properties) {
        var rows = new ArrayList<Ae2StatePropertyData>();
        properties.forEach((name, matcher) -> {
            var row = new Ae2StatePropertyData().setName(name);
            switch (matcher) {
                case PropertyValueMatcher.SingleValue single -> row.setValue(single.value());
                case PropertyValueMatcher.MultiValue multiple -> row.setMode(Ae2StatePropertyData.Mode.MULTIPLE)
                        .setValues(new ArrayList<>(multiple.values()));
                case PropertyValueMatcher.Range range -> row.setMode(Ae2StatePropertyData.Mode.RANGE)
                        .setMin(range.min()).setMax(range.max());
            }
            rows.add(row);
        });
        return rows;
    }

    private static List<Ae2StatePropertyData> importAppliers(Map<String, String> properties) {
        var rows = new ArrayList<Ae2StatePropertyData>();
        properties.forEach((name, value) -> rows.add(new Ae2StatePropertyData().setName(name).setValue(value)));
        return rows;
    }
}
