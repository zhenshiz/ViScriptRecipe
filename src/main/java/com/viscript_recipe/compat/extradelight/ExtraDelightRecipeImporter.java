package com.viscript_recipe.compat.extradelight;

import com.lance5057.extradelight.recipe.*;
import com.lance5057.extradelight.workstations.chiller.ChillerRecipe;
import com.lance5057.extradelight.workstations.doughshaping.recipes.DoughShapingRecipe;
import com.lance5057.extradelight.workstations.dryingrack.DryingRackRecipe;
import com.lance5057.extradelight.workstations.evaporator.recipes.EvaporatorRecipe;
import com.lance5057.extradelight.workstations.juicer.JuicerRecipe;
import com.lance5057.extradelight.workstations.meltingpot.MeltingPotRecipe;
import com.lance5057.extradelight.workstations.mixingbowl.recipes.MixingBowlRecipe;
import com.lance5057.extradelight.workstations.mortar.recipes.MortarRecipe;
import com.lance5057.extradelight.workstations.oven.recipes.OvenRecipe;
import com.lance5057.extradelight.workstations.vat.recipes.VatRecipe;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.compat.extradelight.data.*;
import com.viscript_recipe.data.FluidIngredientData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.importFluid;

/** 在通用原版和农夫乐事导入器之前处理 Extra Delight 原生配方，保留子类加工行为。 */
public final class ExtraDelightRecipeImporter implements RecipeImportHandler {
    public static final ExtraDelightRecipeImporter INSTANCE = new ExtraDelightRecipeImporter();
    private ExtraDelightRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && List.of(MortarRecipe.class, MixingBowlRecipe.class, OvenRecipe.class,
                DryingRackRecipe.class, DoughShapingRecipe.class, ToolOnBlockRecipe.class, FeastRecipe.class,
                MeltingPotRecipe.class, ChillerRecipe.class, VatRecipe.class, EvaporatorRecipe.class,
                BottleFluidRegistryRecipe.class, ShapedWithJarRecipe.class, JuicerRecipe.class,
                DynamicJamRecipe.class, DynamicToastRecipe.class).stream().anyMatch(c -> c.isInstance(holder.value()));
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        ExtraDelightRecipeData data; String type;
        var recipe = holder.value();
        var ops = RegistryOps.create(JsonOps.INSTANCE, provider);
        switch (recipe) {
            case MortarRecipe r -> { type = "mortar"; data = new ExtraDelightMortarRecipeData().setGroup(r.getGroup()).setResult(r.getResultItem(provider).copy()).setFluidOutput(r.getFluid().copy()).setGrinds(r.getGrinds()); }
            case MixingBowlRecipe r -> {
                type = "mixing_bowl";
                var d = new ExtraDelightMixingBowlRecipeData().setGroup(r.getGroup()).setResult(r.getResultItem(provider).copy())
                        .setStirs(r.getStirs()).setContainer(r.getContainer().copy()).setUtensil(preview(r.getUtensil()));
                d.setUtensilCondition(condition(r.getUtensil(), provider));
                var fluids = new ArrayList<FluidIngredientData>();
                var conditions = new ArrayList<String>();
                for (var f : r.getFluids()) { fluids.add(importFluid(f)); conditions.add(fluidCondition(f, provider)); }
                d.setFluidConditions(conditions);
                d.setFluids(fluids); data = d;
            }
            case OvenRecipe r -> { type = "oven"; data = new ExtraDelightOvenRecipeData().setGroup(r.getGroup()).setResult(r.getResultItem(provider).copy()).setContainer(r.getContainerOverride().copy()).setTime(r.getCookTime()).setExperience(r.getExperience()).setConsumeContainer(r.shouldConsumeContainer()); }
            case DryingRackRecipe r -> {
                type = "drying_rack";
                var json = Recipe.CODEC.encodeStart(ops, r).getOrThrow().getAsJsonObject();
                data = new ExtraDelightDryingRackRecipeData().setGroup(r.getGroup()).setResult(r.getResultItem(provider).copy()).setTime(r.getCookingTime()).setExperience(json.has("experience") ? json.get("experience").getAsFloat() : 0);
            }
            case DoughShapingRecipe r -> { type = "dough_shaping"; data = new ExtraDelightDoughShapingRecipeData().setGroup(r.getGroup()).setResult(r.getResultItem(provider).copy()); }
            case ToolOnBlockRecipe r -> {
                type = "tool_on_block";
                var d = new ExtraDelightToolOnBlockRecipeData().setResult(new ItemStack(r.getOut()));
                d.setIngredients(new ArrayList<>(List.of(RecipeIngredient.item(r.getIn()), preview(r.getTool()))));
                d.setIngredientConditions(new ArrayList<>(List.of("", condition(r.getTool(), provider)))); data = d;
            }
            case FeastRecipe r -> {
                type = "feast"; var d = new ExtraDelightFeastRecipeData().setGroup(r.getGroup()).setFeast(r.getFeastStack().copy()).setResult(r.getResultItem(provider).copy());
                d.setIngredients(new ArrayList<>(List.of(preview(r.getContainer()))));
                d.setIngredientConditions(new ArrayList<>(List.of(condition(r.getContainer(), provider)))); data = d;
            }
            case MeltingPotRecipe r -> {
                type = "melting_pot"; var d = new ExtraDelightMeltingPotRecipeData().setGroup(r.getGroup()).setTime(r.cooktime).setFluidOutput(r.result.copy());
                d.setIngredients(new ArrayList<>(List.of(preview(r.input))));
                d.setIngredientConditions(new ArrayList<>(List.of(condition(r.input, provider)))); data = d;
            }
            case ChillerRecipe r -> { type = "chiller"; data = new ExtraDelightChillerRecipeData().setGroup(r.getGroup()).setResult(r.getResultItem(provider).copy()).setFluid(r.getFluid().copy()).setContainer(r.getContainerOverride().copy()).setTime(r.getCookTime()).setExperience(r.getExperience()).setConsumeContainer(r.shouldConsumeContainer()); }
            case VatRecipe r -> {
                type = "vat"; var d = new ExtraDelightVatRecipeData().setGroup(r.getGroup()).setResult(r.getResultItem(provider).copy()).setContainer(r.getUsedItem().copy()).setFluid(importFluid(r.getFluid()));
                var stages = new ArrayList<ExtraDelightVatStageData>();
                for (var s : r.getStageIngredients()) stages.add(new ExtraDelightVatStageData().setIngredient(preview(s.ingredient)).setIngredientCondition(condition(s.ingredient, provider)).setTime(s.time).setLid(s.lid));
                d.setStages(stages); data = d;
            }
            case EvaporatorRecipe r -> { type = "evaporator"; data = new ExtraDelightEvaporatorRecipeData().setGroup(r.getGroup()).setFluid(importFluid(r.getFluid())).setTime(r.getCookTime()).setResult(r.getResultItem(provider).copy()).setLootTable(r.getOutput()).setDisplayBlock(r.getDisplay()); }
            case BottleFluidRegistryRecipe r -> {
                type = "bottle_fluid"; var d = new ExtraDelightBottleFluidRecipeData().setGroup(r.getGroup()).setFluid(importFluid(r.getFluid()));
                d.setIngredients(new ArrayList<>(List.of(preview(r.getBottle()))));
                d.setIngredientConditions(new ArrayList<>(List.of(condition(r.getBottle(), provider)))); data = d;
            }
            case ShapedWithJarRecipe r -> {
                type = "shaped_jar";
                for (var ingredient : r.getIngredients()) {
                    if (ingredient.isCustom() || ingredient.getValues().length > 1)
                        throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_ingredient");
                }
                var pattern = RecipeImporter.importShapedPattern(r.getIngredients(), r.getWidth(), r.getHeight());
                var fluids = new ArrayList<FluidStack>();
                for (var f : Recipe.CODEC.encodeStart(ops, r).getOrThrow().getAsJsonObject().getAsJsonArray("fluids"))
                    fluids.add(FluidStack.OPTIONAL_CODEC.parse(ops, f).getOrThrow());
                data = new ExtraDelightShapedJarRecipeData().setGroup(r.getGroup()).setCategory(r.category()).setShowNotification(r.showNotification()).setResult(r.getResultItem(provider).copy()).setPattern(new ArrayList<>(pattern.pattern())).setKey(new ArrayList<>(pattern.key())).setFluids(fluids);
            }
            case JuicerRecipe r -> { type = "juicer"; data = new ExtraDelightJuicerRecipeData().setGroup(r.getGroup()).setResult(r.getResultItem(provider).copy()).setFluidOutput(r.getFluid().copy()).setChance(r.getChance()); }
            case DynamicJamRecipe r -> { type = "dynamic_jam"; data = new ExtraDelightDynamicJamRecipeData().setGroup(r.getGroup()).setGraphic(r.getGraphic()).setResult(r.getResultItem(provider).copy()).setContainer(r.getContainerOverride().copy()).setTime(r.getCookTime()).setExperience(r.getExperience()).setRecipeBookTab(r.getRecipeBookTab() == null ? "meals" : r.getRecipeBookTab().getSerializedName()); }
            case DynamicToastRecipe r -> { type = "dynamic_toast"; data = new ExtraDelightDynamicToastRecipeData().setGroup(r.getGroup()).setGraphic(r.getGraphic()).setResult(r.getResultItem(provider).copy()).setCategory(r.category()); }
            default -> { return null; }
        }
        switch (recipe) {
            case VatRecipe r -> data.setFluidConditions(new ArrayList<>(List.of(fluidCondition(r.getFluid(), provider))));
            case EvaporatorRecipe r -> data.setFluidConditions(new ArrayList<>(List.of(fluidCondition(r.getFluid(), provider))));
            case BottleFluidRegistryRecipe r -> data.setFluidConditions(new ArrayList<>(List.of(fluidCondition(r.getFluid(), provider))));
            default -> { }
        }
        if (!List.of("tool_on_block", "feast", "melting_pot", "bottle_fluid", "evaporator", "shaped_jar").contains(type)) {
            var values = new ArrayList<RecipeIngredient>(); var conditions = new ArrayList<String>();
            for (var ingredient : recipe.getIngredients()) { values.add(preview(ingredient)); conditions.add(condition(ingredient, provider)); }
            // 数据模型已实现接口的可变原料列表，使用接口修改材料，无需另一套导入 API。
            data.getIngredients().clear(); data.getIngredients().addAll(values); data.setIngredientConditions(conditions);
        }
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), ExtraDelightRecipeEditorTypes.id(type)).setData(data));
    }

    private static RecipeIngredient preview(Ingredient ingredient) throws RecipeImportException {
        if (ingredient.isEmpty()) return RecipeIngredient.empty();
        if (!ingredient.isCustom() && ingredient.getValues().length == 1) return RecipeImporter.importIngredient(ingredient);
        return ingredient.getItems().length == 0 ? RecipeIngredient.empty() : RecipeIngredient.item(ingredient.getItems()[0]);
    }

    private static String condition(Ingredient ingredient, HolderLookup.Provider provider) throws RecipeImportException {
        if (ingredient.isEmpty()) return "";
        var ops = RegistryOps.create(JsonOps.INSTANCE, provider);
        var nativeJson = Ingredient.CODEC.encodeStart(ops, ingredient).getOrThrow();
        var editableJson = Ingredient.CODEC.encodeStart(ops, preview(ingredient).compile()).getOrThrow();
        return nativeJson.equals(editableJson) ? "" : nativeJson.toString();
    }

    private static String fluidCondition(net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient ingredient, HolderLookup.Provider provider) throws RecipeImportException {
        var ops = RegistryOps.create(JsonOps.INSTANCE, provider);
        var codec = net.neoforged.neoforge.fluids.crafting.FluidIngredient.CODEC;
        var nativeJson = codec.encodeStart(ops, ingredient.ingredient()).getOrThrow();
        var editableJson = codec.encodeStart(ops, ExtraDelightRecipeSupport.sizedFluid(importFluid(ingredient)).ingredient()).getOrThrow();
        return nativeJson.equals(editableJson) ? "" : nativeJson.toString();
    }
}
