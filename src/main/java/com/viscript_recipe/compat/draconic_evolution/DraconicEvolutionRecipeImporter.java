package com.viscript_recipe.compat.draconic_evolution;

import com.brandon3055.draconicevolution.api.crafting.FusionRecipe;
import com.brandon3055.draconicevolution.api.crafting.StackIngredient;
import com.viscript_recipe.compat.draconic_evolution.data.DraconicFusionIngredientData;
import com.viscript_recipe.compat.draconic_evolution.data.DraconicFusionRecipeData;
import com.viscript_recipe.compat.draconic_evolution.data.DraconicIngredientData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.ComponentStackIngredient;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import java.util.ArrayList;

public final class DraconicEvolutionRecipeImporter implements RecipeImportHandler {
    public static final DraconicEvolutionRecipeImporter INSTANCE = new DraconicEvolutionRecipeImporter();
    private DraconicEvolutionRecipeImporter() {}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        // 自定义 IFusionRecipe 可能重写匹配或逐刻处理逻辑，不能用普通配方替换这些语义。
        return holder != null && holder.value().getClass() == FusionRecipe.class;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        if (!canImport(holder)) return null;
        var recipe = (FusionRecipe) holder.value();
        var injectors = new ArrayList<DraconicFusionIngredientData>();
        for (var input : recipe.fusionIngredients()) {
            injectors.add(new DraconicFusionIngredientData().setIngredient(importIngredient(input.get())).setConsume(input.consume()));
        }
        var data = new DraconicFusionRecipeData()
                .setCatalyst(importIngredient(recipe.getCatalyst()))
                .setInjectors(injectors)
                .setResult(RecipeImporter.copyResult(recipe, provider))
                .setTotalEnergy(recipe.getEnergyCost())
                .setTechLevel(recipe.getRecipeTier());
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(),
                DraconicEvolutionRecipeEditorTypes.FUSION_CRAFTING).setData(data));
    }

    public static DraconicIngredientData importIngredient(Ingredient ingredient) throws RecipeImportException {
        if (ingredient == null || ingredient.isEmpty()) {
            throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
        }
        var data = new DraconicIngredientData();
        var custom = ingredient.getCustomIngredient();
        if (custom instanceof StackIngredient counted) {
            if (counted.getCount() < 1) throw unsupported();
            data.setCount(counted.getCount()).setStackIngredient(true);
            var tag = counted.items().unwrapKey();
            if (tag.isPresent()) data.getAlternatives().add(RecipeIngredient.tag(tag.get().location()));
            else counted.items().forEach(item -> data.getAlternatives().add(RecipeIngredient.item(item.value())));
        } else if (custom instanceof CompoundIngredient compound) {
            for (var child : compound.children()) {
                var imported = importIngredient(child);
                // 复合原料内部的数量原料，与龙之进化带数量催化剂的消耗语义不同。
                if (imported.isStackIngredient()) throw unsupported();
                data.getAlternatives().addAll(imported.getAlternatives());
            }
        } else if (custom instanceof DataComponentIngredient components) {
            // RecipeIngredient 通过含数据组件的 ItemStack 表示严格匹配，无法保留非严格匹配语义。
            if (!components.isStrict()) throw unsupported();
            components.getItems().forEach(stack -> addStack(data, stack));
        } else if (custom instanceof ComponentStackIngredient components) {
            components.getItems().forEach(stack -> addStack(data, stack));
        } else if (custom != null) {
            throw unsupported();
        } else {
            for (var value : ingredient.getValues()) {
                if (value instanceof Ingredient.ItemValue item) addStack(data, item.item());
                else if (value instanceof Ingredient.TagValue tag) data.getAlternatives().add(RecipeIngredient.tag(tag.tag().location()));
                else throw unsupported();
            }
        }
        if (data.isEmpty()) throw unsupported();
        return data;
    }

    private static void addStack(DraconicIngredientData data, ItemStack stack) {
        if (!stack.isEmpty()) data.getAlternatives().add(RecipeIngredient.item(stack.copyWithCount(1)));
    }

    private static RecipeImportException unsupported() {
        return new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_ingredient");
    }
}
