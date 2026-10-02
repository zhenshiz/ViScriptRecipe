package com.viscript_recipe.compat.eidolon;

import alexthw.eidolon_repraised.recipe.*;
import com.viscript_recipe.compat.eidolon.data.*;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.ComponentStackIngredient;
import com.viscript_recipe.recipe.importer.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import java.util.ArrayList;
import java.util.List;

/** 仅导入编辑器能够完整表达其行为的原生配方类。 */
public final class EidolonRecipeImporter implements RecipeImportHandler {
    public static final EidolonRecipeImporter INSTANCE = new EidolonRecipeImporter();
    private EidolonRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        if (holder == null) return false;
        var type = holder.value().getClass();
        return type == CrucibleRecipe.class || type == WorktableRecipe.class || type == DyeRecipe.class
                || type == GenericRitualRecipe.class || type == ItemRitualRecipe.class || type == SummonRitualRecipe.class
                || type == LocationRitualRecipe.class || type == CommandRitualRecipe.class;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (!canImport(holder)) return null;
        if (holder.value() instanceof CrucibleRecipe recipe) {
            var steps = new ArrayList<EidolonCrucibleStepData>();
            for (var step : recipe.getSteps()) steps.add(new EidolonCrucibleStepData()
                    .setStirs(step.stirs()).setItems(importIngredients(step.matches())));
            return success(holder, EidolonRecipeEditorTypes.CRUCIBLE, new EidolonCrucibleRecipeData()
                    .setSteps(steps).setResult(RecipeImporter.copyResult(recipe, provider)));
        }
        if (holder.value() instanceof WorktableRecipe recipe) {
            if (recipe.pattern_core.width() > 3 || recipe.pattern_core.height() > 3 || recipe.getOuter().size() != 4) {
                throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_type", holder.id());
            }
            var data = new EidolonWorktableRecipeData().setCore(EidolonWorktableRecipeData.emptySlots(9)).setWidth(recipe.pattern_core.width()).setHeight(recipe.pattern_core.height())
                    .setReagents(importIngredients(recipe.getOuter())).setResult(RecipeImporter.copyResult(recipe, provider));
            for (int y = 0; y < data.getHeight(); y++) for (int x = 0; x < data.getWidth(); x++) {
                data.getCore().set(y * 3 + x, importIngredient(recipe.getCore().get(y * data.getWidth() + x)));
            }
            return success(holder, EidolonRecipeEditorTypes.WORKTABLE, data);
        }
        if (holder.value() instanceof DyeRecipe recipe) {
            return success(holder, EidolonRecipeEditorTypes.DYE, new EidolonDyeRecipeData()
                    .setInputs(importIngredients(recipe.getIngredients())).setGroup(recipe.getGroup()).setCategory(recipe.category())
                    .setResult(RecipeImporter.copyResult(recipe, provider)));
        }
        var recipe = (RitualRecipe) holder.value();
        var data = new EidolonRitualRecipeData().setReagent(importIngredient(recipe.getReagent()))
                .setPedestals(importIngredients(recipe.getPedestalItems())).setFoci(importIngredients(recipe.getFocusItems()))
                .setInvariants(importIngredients(recipe.getInvariantItems())).setHealthRequirement(recipe.getHealthRequirement());
        ResourceLocation type;
        if (recipe instanceof GenericRitualRecipe generic) {
            data.setRitual(generic.getId()); type = EidolonRecipeEditorTypes.GENERIC_RITUAL;
        } else if (recipe instanceof ItemRitualRecipe item) {
            data.setResult(item.getResult().copy()).setSymbol(item.getSymbol()).setColor(item.getColor())
                    .setKeepReagentComponents(item.keepsComponent()); type = EidolonRecipeEditorTypes.ITEM_RITUAL;
        } else if (recipe instanceof SummonRitualRecipe summon) {
            data.setEntity(summon.getEntityRL()).setSummonCount(summon.getCount()); type = EidolonRecipeEditorTypes.SUMMON_RITUAL;
        } else if (recipe instanceof LocationRitualRecipe locate) {
            data.setStructureTag(locate.getStructureTagKey()); type = EidolonRecipeEditorTypes.LOCATION_RITUAL;
        } else if (recipe instanceof CommandRitualRecipe command) {
            data.setCommands(new ArrayList<>(command.getCommands())).setSymbol(command.getSymbol()).setColor(command.getColor());
            type = EidolonRecipeEditorTypes.COMMAND_RITUAL;
        } else return null;
        return success(holder, type, data);
    }

    private static RecipeImportResult success(RecipeHolder<?> holder, ResourceLocation type, com.viscript_recipe.data.IVSRecipeData data) {
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), type).setData(data));
    }

    public static List<EidolonIngredientData> importIngredients(List<Ingredient> ingredients) throws RecipeImportException {
        var result = new ArrayList<EidolonIngredientData>();
        for (var ingredient : ingredients) result.add(importIngredient(ingredient));
        return result;
    }

    public static EidolonIngredientData importIngredient(Ingredient ingredient) throws RecipeImportException {
        var data = new EidolonIngredientData();
        if (ingredient == null || ingredient.isEmpty()) return data;
        var custom = ingredient.getCustomIngredient();
        if (custom instanceof CompoundIngredient compound) {
            for (var child : compound.children()) data.getAlternatives().addAll(importIngredient(child).getAlternatives());
        } else if (custom instanceof DataComponentIngredient components) {
            var tag = components.items().unwrapKey();
            if (tag.isPresent()) {
                var alternative = EidolonIngredientValueData.of(RecipeIngredient.tag(tag.get().location()))
                        .setMatchComponents(true).setStrict(components.isStrict());
                alternative.setPredicate(components.components());
                data.getAlternatives().add(alternative);
            } else components.getItems().forEach(stack -> {
                var alternative = EidolonIngredientValueData.of(RecipeIngredient.item(stack.copyWithCount(1)))
                        .setMatchComponents(true).setStrict(components.isStrict());
                alternative.setPredicate(components.components());
                data.getAlternatives().add(alternative);
            });
        } else if (custom instanceof ComponentStackIngredient components) {
            components.getItems().forEach(stack -> data.getAlternatives().add(EidolonIngredientValueData.of(RecipeIngredient.item(stack.copyWithCount(1)))));
        } else if (custom != null) throw unsupported();
        else for (var value : ingredient.getValues()) {
            if (value instanceof Ingredient.ItemValue item) data.getAlternatives().add(EidolonIngredientValueData.of(RecipeIngredient.item(item.item().copyWithCount(1))));
            else if (value instanceof Ingredient.TagValue tag) data.getAlternatives().add(EidolonIngredientValueData.of(RecipeIngredient.tag(tag.tag().location())));
            else throw unsupported();
        }
        if (data.isEmpty()) throw unsupported();
        return data;
    }

    private static RecipeImportException unsupported() {
        return new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_ingredient");
    }
}
