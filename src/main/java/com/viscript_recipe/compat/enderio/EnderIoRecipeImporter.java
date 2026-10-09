package com.viscript_recipe.compat.enderio;

import com.enderio.enderio.content.enchanter.EnchanterRecipe;
import com.enderio.enderio.content.fire_crafting.FireCraftingRecipe;
import com.enderio.enderio.content.machines.alloy.AlloySmeltingRecipe;
import com.enderio.enderio.content.machines.obelisks.weather.WeatherChangeRecipe;
import com.enderio.enderio.content.machines.sag_mill.SagMillingRecipe;
import com.enderio.enderio.content.machines.slicer.SlicingRecipe;
import com.enderio.enderio.content.machines.soul_binder.SoulBindingRecipe;
import com.enderio.enderio.content.machines.vat.FermentingRecipe;
import com.enderio.enderio.content.storage.fluid_tank.TankRecipe;
import com.enderio.enderio.foundation.soul.ShapedEntityStorageRecipe;
import com.viscript_recipe.compat.enderio.data.EnderIoFluidData;
import com.viscript_recipe.compat.enderio.data.EnderIoIngredientData;
import com.viscript_recipe.compat.enderio.data.EnderIoOutputData;
import com.viscript_recipe.compat.enderio.data.EnderIoRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

public final class EnderIoRecipeImporter implements RecipeImportHandler {
    public static final EnderIoRecipeImporter INSTANCE = new EnderIoRecipeImporter();
    private static final List<Class<?>> CLASSES = List.of(FireCraftingRecipe.class, AlloySmeltingRecipe.class,
            EnchanterRecipe.class, SagMillingRecipe.class, SlicingRecipe.class, SoulBindingRecipe.class,
            TankRecipe.class, FermentingRecipe.class, WeatherChangeRecipe.class, ShapedEntityStorageRecipe.class);
    private EnderIoRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) { return holder != null && CLASSES.contains(holder.value().getClass()); }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (!canImport(holder)) return null;
        var data = new EnderIoRecipeData();
        String type;
        try {
            switch (holder.value()) {
                case AlloySmeltingRecipe recipe -> {
                    type = "alloy_smelting";
                    for (var input : recipe.inputs()) data.getInputs().add(EnderIoIngredientData.from(input.ingredient(), provider).setCount(input.count()));
                    output(data, recipe.output());
                    data.setEnergy(recipe.energy()).setExperience(recipe.experience()).setSmelting(recipe.isSmelting());
                }
                case SagMillingRecipe recipe -> {
                    type = "sag_milling";
                    input(data, recipe.input(), provider);
                    for (var result : recipe.outputs()) {
                        var output = new EnderIoOutputData().setChance(result.chance()).setOptional(result.isOptional());
                        result.output().ifLeft(stack -> output.setItem(stack.copy())).ifRight(tag -> output.setUseTag(true).setTag(tag.itemTag().location()).setCount(tag.count()));
                        data.getOutputs().add(output);
                    }
                    data.setEnergy(recipe.energy()).setBonus(recipe.bonusType().getSerializedName());
                }
                case SlicingRecipe recipe -> {
                    type = "slicing";
                    for (var input : recipe.inputs()) input(data, input, provider);
                    output(data, recipe.output()); data.setEnergy(recipe.energy());
                }
                case SoulBindingRecipe recipe -> {
                    type = "soul_binding";
                    input(data, recipe.input(), provider); output(data, recipe.output());
                    data.setEnergy(recipe.energy()).setExperienceLevels(recipe.experience())
                            .setEntityType(recipe.entityType().map(Object::toString).orElse(""))
                            .setMobCategory(recipe.mobCategory().map(c -> c.getSerializedName()).orElse(""))
                            .setSoulData(recipe.soulData().orElse("")).setCopyInputComponents(recipe.copyInputComponents());
                }
                case TankRecipe recipe -> {
                    type = "tank";
                    input(data, recipe.input(), provider); output(data, recipe.output());
                    data.setFluidInput(EnderIoFluidData.from(recipe.fluid(), provider)).setTankMode(recipe.mode().getSerializedName());
                }
                case FermentingRecipe recipe -> {
                    type = "vat_fermenting";
                    input(data, Ingredient.of(recipe.firstReagent()), provider); input(data, Ingredient.of(recipe.secondReagent()), provider);
                    data.setFluidInput(EnderIoFluidData.from(recipe.input(), provider)).setFluidOutput(recipe.output().copy()).setTicks(recipe.ticks());
                }
                case WeatherChangeRecipe recipe -> {
                    type = "weather_change";
                    data.setFluidOutput(recipe.fluid().copy()).setWeather(recipe.mode().getSerializedName());
                }
                case EnchanterRecipe recipe -> {
                    type = "enchanting";
                    data.getInputs().add(EnderIoIngredientData.from(recipe.input().ingredient(), provider).setCount(recipe.input().count()));
                    data.setEnchantment(recipe.enchantment().unwrapKey().orElseThrow().location()).setCostMultiplier(recipe.costMultiplier());
                }
                case FireCraftingRecipe recipe -> {
                    type = "fire_crafting";
                    for (var result : recipe.results()) data.getOutputs().add(new EnderIoOutputData().setItem(result.result().copy())
                            .setChance(result.chance()).setMinCount(result.minCount()).setMaxCount(result.maxCount()));
                    data.setBases(new ArrayList<>(recipe.bases().stream().map(BuiltInRegistries.BLOCK::getKey).toList()))
                            .setBaseTags(new ArrayList<>(recipe.baseTags().stream().map(t -> t.location()).toList()))
                            .setDimensions(new ArrayList<>(recipe.dimensions().stream().map(t -> t.location()).toList()))
                            .setBlockAfterBurning(recipe.blockAfterBurning().map(BuiltInRegistries.BLOCK::getKey).map(Object::toString).orElse(""));
                }
                case ShapedEntityStorageRecipe recipe -> {
                    type = "shaped_entity_storage";
                    var wrapped = recipe.getWrapped();
                    data.setWidth(wrapped.getWidth()).setHeight(wrapped.getHeight()).setGroup(wrapped.getGroup())
                            .setCategory(wrapped.category()).setShowNotification(wrapped.showNotification());
                    if (data.getWidth() > 3 || data.getHeight() > 3) throw new IllegalArgumentException("网格超过 3×3");
                    for (int i = 0; i < 9; i++) data.getInputs().add(new EnderIoIngredientData());
                    for (int y = 0; y < data.getHeight(); y++) for (int x = 0; x < data.getWidth(); x++)
                        data.getInputs().set(y * 3 + x, EnderIoIngredientData.from(wrapped.getIngredients().get(y * data.getWidth() + x), provider));
                    output(data, wrapped.getResultItem(provider));
                }
                default -> { return null; }
            }
            if (data.getOutputs().size() > 16 || data.getInputs().size() > 9) throw new IllegalArgumentException("槽位数量超过编辑器限制");
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), EnderIoRecipeEditorTypes.id(type)).setData(data));
        } catch (RuntimeException exception) {
            throw new RecipeImportException("viscript_recipe.editor.enderio.import_error", holder.id(), exception.getMessage());
        }
    }

    private static void input(EnderIoRecipeData data, Ingredient ingredient, HolderLookup.Provider provider) {
        data.getInputs().add(EnderIoIngredientData.from(ingredient, provider));
    }
    private static void output(EnderIoRecipeData data, ItemStack stack) { data.getOutputs().add(new EnderIoOutputData().setItem(stack.copy())); }
}
