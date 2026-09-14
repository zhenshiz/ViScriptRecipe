package com.viscript_recipe.compat.mysticalagriculture;

import com.blakebr0.mysticalagriculture.api.crafting.*;
import com.blakebr0.mysticalagriculture.crafting.recipe.AwakeningRecipe;
import com.blakebr0.mysticalagriculture.crafting.recipe.InfusionRecipe;
import com.viscript_recipe.compat.mysticalagriculture.data.*;
import com.viscript_recipe.mixin.MysticalAgricultureAwakeningRecipeAccessor;
import com.viscript_recipe.mixin.MysticalAgricultureInfusionRecipeAccessor;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;

/**
 * Imports the six Mystical Agriculture recipe serializer variants backed by Recipe Codecs.
 */
public final class MysticalAgricultureRecipeImporter implements RecipeImportHandler {
    public static final MysticalAgricultureRecipeImporter INSTANCE = new MysticalAgricultureRecipeImporter();

    private MysticalAgricultureRecipeImporter() {
    }

    @Override
    public boolean canImport(RecipeHolder<?> holder) {
        if (holder == null) {
            return false;
        }
        var recipe = holder.value();
        return recipe instanceof IInfusionRecipe
                || recipe instanceof IAwakeningRecipe
                || recipe instanceof IEnchanterRecipe
                || recipe instanceof IReprocessorRecipe
                || recipe instanceof ISoulExtractionRecipe
                || recipe instanceof ISouliumSpawnerRecipe;
    }

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) throws RecipeImportException {
        var recipe = holder.value();
        if (recipe instanceof IAwakeningRecipe awakening) {
            var data = new MysticalAgricultureAwakeningRecipeData()
                    .setInput(RecipeImporter.importIngredient(awakening.getAltarIngredient()))
                    .setResult(RecipeImporter.copyResult(awakening, provider))
                    .setTransferComponents(recipe instanceof AwakeningRecipe concrete
                            && ((MysticalAgricultureAwakeningRecipeAccessor) concrete).viscriptRecipe$getTransferComponents());
            var ingredients = awakening.getIngredients();
            for (int index = 0; index < MysticalAgricultureAwakeningRecipeData.PEDESTAL_INGREDIENT_COUNT; index++) {
                var sourceIndex = index * 2 + 1;
                if (sourceIndex < ingredients.size() && !ingredients.get(sourceIndex).isEmpty()) {
                    data.setIngredient(index, RecipeImporter.importIngredient(ingredients.get(sourceIndex)));
                }
                var essence = awakening.getEssences().size() > index ? awakening.getEssences().get(index) : ItemStack.EMPTY;
                data.setEssence(index, RecipeImporter.copyStack(essence));
            }
            return success(holder, MysticalAgricultureRecipeEditorTypes.AWAKENING,
                    entry -> entry.setData(data));
        }
        if (recipe instanceof IInfusionRecipe infusion) {
            var data = new MysticalAgricultureInfusionRecipeData()
                    .setInput(RecipeImporter.importIngredient(infusion.getAltarIngredient()))
                    .setResult(RecipeImporter.copyResult(infusion, provider))
                    .setTransferComponents(recipe instanceof InfusionRecipe concrete
                            && ((MysticalAgricultureInfusionRecipeAccessor) concrete).viscriptRecipe$getTransferComponents());
            for (int index = 0; index < Math.min(MysticalAgricultureInfusionRecipeData.MAX_PEDESTAL_INGREDIENTS,
                    infusion.getIngredients().size()); index++) {
                var ingredient = infusion.getIngredients().get(index);
                if (!ingredient.isEmpty()) {
                    data.setIngredient(index, RecipeImporter.importIngredient(ingredient));
                }
            }
            return success(holder, MysticalAgricultureRecipeEditorTypes.INFUSION,
                    entry -> entry.setData(data));
        }
        if (recipe instanceof IEnchanterRecipe enchanter) {
            var enchantmentId = enchanter.getEnchantment().unwrapKey().map(ResourceKey::location).orElse(null);
            if (enchantmentId == null) {
                throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.unsupported_type", "unbound Mystical Agriculture enchantment");
            }
            var data = new MysticalAgricultureEnchanterRecipeData().setEnchantment(enchantmentId);
            for (int index = 0; index < Math.min(MysticalAgricultureEnchanterRecipeData.MAX_INGREDIENTS,
                    enchanter.getIngredients().size()); index++) {
                data.setIngredient(index, RecipeImporter.importIngredient(enchanter.getIngredients().get(index))
                        .setCount(enchanter.getCount(index)));
            }
            return success(holder, MysticalAgricultureRecipeEditorTypes.ENCHANTER,
                    entry -> entry.setData(data));
        }
        if (recipe instanceof IReprocessorRecipe reprocessor) {
            if (reprocessor.getIngredients().isEmpty()) {
                throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
            }
            var data = new MysticalAgricultureReprocessorRecipeData()
                    .setInput(RecipeImporter.importIngredient(reprocessor.getIngredients().getFirst()))
                    .setResult(RecipeImporter.copyResult(reprocessor, provider));
            return success(holder, MysticalAgricultureRecipeEditorTypes.REPROCESSOR,
                    entry -> entry.setData(data));
        }
        if (recipe instanceof ISoulExtractionRecipe extraction) {
            if (extraction.getIngredients().isEmpty() || extraction.getMobSoulType() == null) {
                throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
            }
            var data = new MysticalAgricultureSoulExtractionRecipeData()
                    .setInput(RecipeImporter.importIngredient(extraction.getIngredients().getFirst()))
                    .setSoulType(extraction.getMobSoulType().getId())
                    .setSouls(extraction.getSouls());
            return success(holder, MysticalAgricultureRecipeEditorTypes.SOUL_EXTRACTION,
                    entry -> entry.setData(data));
        }
        if (recipe instanceof ISouliumSpawnerRecipe spawner) {
            if (spawner.getIngredients().isEmpty()) {
                throw new RecipeImportException("viscript_recipe.editor.import_recipe.error.empty_ingredient");
            }
            var entities = new ArrayList<MysticalAgricultureWeightedEntityData>();
            for (var entry : spawner.getEntityTypes().unwrap()) {
                var id = BuiltInRegistries.ENTITY_TYPE.getKey(entry.data());
                entities.add(new MysticalAgricultureWeightedEntityData()
                        .setEntity(id)
                        .setWeight(Math.max(1, entry.weight().asInt())));
            }
            var data = new MysticalAgricultureSouliumSpawnerRecipeData()
                    .setInput(RecipeImporter.importIngredient(spawner.getIngredients().getFirst()).setCount(spawner.getCount(0)))
                    .setEntities(entities);
            return success(holder, MysticalAgricultureRecipeEditorTypes.SOULIUM_SPAWNER,
                    entry -> entry.setData(data));
        }
        return null;
    }

    private static RecipeImportResult success(
            RecipeHolder<?> holder,
            ResourceLocation type,
            java.util.function.Consumer<com.viscript_recipe.data.RecipeEntry> consumer
    ) {
        var entry = RecipeImporter.baseEntry(holder.id(), type);
        consumer.accept(entry);
        return RecipeImporter.success(entry);
    }
}
