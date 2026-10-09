package com.viscript_recipe.compat.justdirethings;

import com.direwolf20.justdirethings.datagen.recipes.*;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.compat.justdirethings.data.JustDireIngredientData;
import com.viscript_recipe.compat.justdirethings.data.JustDireSmithingData;
import com.viscript_recipe.compat.justdirethings.data.JustDireTransformationData;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import static com.viscript_recipe.compat.justdirethings.JustDireRecipeEditorTypes.id;

/** 导入三类世界转化与两类原生锻造配方；掉落展示不属于可编辑配方。 */
public final class JustDireRecipeImporter implements RecipeImportHandler {
    public static final JustDireRecipeImporter INSTANCE = new JustDireRecipeImporter();
    private JustDireRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && (holder.value() instanceof GooSpreadRecipe || holder.value() instanceof GooSpreadRecipeTag
                || holder.value() instanceof FluidDropRecipe || holder.value() instanceof AbilityRecipe || holder.value() instanceof PaxelRecipe);
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        IVSRecipeData data; String type;
        switch (holder.value()) {
            case GooSpreadRecipe r -> {
                type = "goospread";
                data = new JustDireTransformationData().setInput(r.getInput()).setOutput(r.getOutput())
                        .setTier(r.getTierRequirement()).setDuration(r.getCraftingDuration()).setNativeId(nativeId(r, provider));
            }
            case GooSpreadRecipeTag r -> {
                type = "goospread_tag";
                data = new JustDireTransformationData().setInputTag(r.getInput().getTag().location()).setOutput(r.getOutput())
                        .setTier(r.getTierRequirement()).setDuration(r.getCraftingDuration()).setNativeId(nativeId(r, provider));
            }
            case FluidDropRecipe r -> {
                type = "fluiddrop";
                data = new JustDireTransformationData().setInput(r.getInput()).setOutput(r.getOutput()).setCatalyst(r.getCatalyst()).setNativeId(r.getId());
            }
            case AbilityRecipe r -> {
                type = "ability";
                data = new JustDireSmithingData().setTemplate(JustDireIngredientData.from(r.getTemplate(), provider))
                        .setBase(JustDireIngredientData.from(r.getBase(), provider)).setAddition(JustDireIngredientData.from(r.getAddition(), provider));
            }
            case PaxelRecipe r -> {
                type = "paxel";
                data = new JustDireSmithingData().setTemplate(JustDireIngredientData.from(r.getTemplate(), provider))
                        .setBase(JustDireIngredientData.from(r.getBase(), provider)).setAddition(JustDireIngredientData.from(r.getAddition(), provider)).setResult(r.getResult());
            }
            default -> { return null; }
        }
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), id(type)).setData(data));
    }

    private static ResourceLocation nativeId(Recipe<?> recipe, HolderLookup.Provider provider) {
        // 原生黏液配方未提供内部 id 的 getter，使用其公开 Codec 读取，避免反射和 Mixin。
        return ResourceLocation.parse(Recipe.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, provider), recipe)
                .getOrThrow().getAsJsonObject().get("id").getAsString());
    }
}
