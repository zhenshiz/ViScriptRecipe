package com.viscript_recipe.compat.bakery;

import com.viscript_recipe.compat.bakery.data.BakeryBakingStationRecipeData;
import com.viscript_recipe.compat.bakery.data.BakeryCakeInteractionRecipeData;
import com.viscript_recipe.compat.farm_and_charm.FarmCharmRecipeImporter;
import com.viscript_recipe.compat.farm_and_charm.data.FarmCharmIngredientData;
import com.viscript_recipe.recipe.importer.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.satisfy.bakery.core.recipe.BakingStationRecipe;
import net.satisfy.bakery.core.recipe.BlankCakeInteractionRecipe;

import java.util.ArrayList;

/** 导入 Bakery 原生烘焙及蛋糕交互配方，材料转换复用现有的候选项处理。 */
public final class BakeryRecipeImporter implements RecipeImportHandler {
    public static final BakeryRecipeImporter INSTANCE = new BakeryRecipeImporter();
    private BakeryRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        return holder != null && (holder.value() instanceof BakingStationRecipe || holder.value() instanceof BlankCakeInteractionRecipe);
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (!canImport(holder)) return null;
        if (holder.value() instanceof BlankCakeInteractionRecipe recipe) {
            var result = recipe.result();
            var data = new BakeryCakeInteractionRecipeData().setStage(recipe.stage().name()).setPriority(recipe.priority())
                    .setInput(FarmCharmRecipeImporter.importIngredient(recipe.ingredient()))
                    .setReplaceBlock(result.setBlock() != null).setPatchState(result.setState() != null)
                    .setReturnItem(result.giveItem() != null).setPlaySound(result.sound() != null)
                    .setConsumeOne(result.consumeOne()).setParticles(result.particles()).setCooldownTicks(result.cooldownTicks());
            if (result.setBlock() != null) data.setBlock(result.setBlock());
            if (result.setState() != null) data.setCake(result.setState().cake()).setCupcake(result.setState().cupcake()).setCookie(result.setState().cookie());
            if (result.giveItem() != null) data.setItem(result.giveItem());
            if (result.sound() != null) data.setSound(result.sound());
            return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), BakeryRecipeEditorTypes.CAKE_INTERACTION).setData(data));
        }
        var recipe = (BakingStationRecipe) holder.value();
        if (recipe.getIngredients().size() > BakeryBakingStationRecipeData.MAX_INPUTS)
            throw new RecipeImportException("viscript_recipe.editor.bakery.too_many_inputs");
        var inputs = new ArrayList<FarmCharmIngredientData>();
        for (var ingredient : recipe.getIngredients()) inputs.add(FarmCharmRecipeImporter.importIngredient(ingredient));
        var data = new BakeryBakingStationRecipeData().setInputs(inputs)
                .setInputSlots(Math.max(2, inputs.size())).setResult(recipe.getResultItem(provider).copy());
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), BakeryRecipeEditorTypes.BAKING_STATION).setData(data));
    }
}
