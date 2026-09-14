package com.viscript_recipe.compat.mysticalagriculture;

import com.blakebr0.cucumber.crafting.ingredient.IngredientWithCount;
import com.blakebr0.mysticalagriculture.crafting.recipe.*;
import com.blakebr0.mysticalagriculture.registry.MobSoulTypeRegistry;
import com.lowdragmc.lowdraglib2.Platform;
import com.viscript_recipe.compat.mysticalagriculture.data.*;
import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;

/**
 * Creates Mystical Agriculture recipe objects from editor-owned data.
 */
public final class MysticalAgricultureRecipeFactory {
    private MysticalAgricultureRecipeFactory() {
    }

    public static Recipe<?> compileInfusion(MysticalAgricultureInfusionRecipeData data) {
        var ingredients = NonNullList.<Ingredient>create();
        for (var ingredientData : data.getIngredients()) {
            if (ingredients.size() >= MysticalAgricultureInfusionRecipeData.MAX_PEDESTAL_INGREDIENTS) {
                break;
            }
            var ingredient = compileIngredient(ingredientData);
            if (!ingredient.isEmpty()) {
                ingredients.add(ingredient);
            }
        }
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Mystical Agriculture infusion recipe must have at least one pedestal ingredient");
        }
        return new InfusionRecipe(
                requireIngredient(data.getInput(), "Mystical Agriculture infusion input cannot be empty"),
                ingredients,
                requireItem(data.getResult(), "Mystical Agriculture infusion result cannot be empty"),
                data.isTransferComponents()
        );
    }

    public static Recipe<?> compileAwakening(MysticalAgricultureAwakeningRecipeData data) {
        var ingredients = NonNullList.withSize(
                MysticalAgricultureAwakeningRecipeData.PEDESTAL_INGREDIENT_COUNT,
                Ingredient.EMPTY
        );
        var ingredientCount = 0;
        for (int index = 0; index < ingredients.size(); index++) {
            var ingredient = compileIngredient(data.ingredient(index));
            ingredients.set(index, ingredient);
            if (!ingredient.isEmpty()) {
                ingredientCount++;
            }
        }
        if (ingredientCount == 0) {
            throw new IllegalArgumentException("Mystical Agriculture awakening recipe must have at least one pedestal ingredient");
        }
        var essences = NonNullList.withSize(
                MysticalAgricultureAwakeningRecipeData.ESSENCE_COUNT,
                ItemStack.EMPTY
        );
        var essenceCount = 0;
        for (int index = 0; index < essences.size(); index++) {
            var essence = normalizeItem(data.essence(index));
            essences.set(index, essence);
            if (!essence.isEmpty()) {
                essenceCount++;
            }
        }
        if (essenceCount == 0) {
            throw new IllegalArgumentException("Mystical Agriculture awakening recipe must have at least one essence");
        }
        return new AwakeningRecipe(
                requireIngredient(data.getInput(), "Mystical Agriculture awakening input cannot be empty"),
                ingredients,
                essences,
                requireItem(data.getResult(), "Mystical Agriculture awakening result cannot be empty"),
                data.isTransferComponents()
        );
    }

    public static Recipe<?> compileEnchanter(MysticalAgricultureEnchanterRecipeData data) {
        var ingredients = NonNullList.<IngredientWithCount>create();
        for (var ingredient : data.getIngredients()) {
            if (ingredients.size() >= MysticalAgricultureEnchanterRecipeData.MAX_INGREDIENTS) {
                break;
            }
            if (ingredient != null && !ingredient.isEmpty()) {
                ingredients.add(compileCountedIngredient(ingredient));
            }
        }
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Mystical Agriculture enchanter recipe must have at least one ingredient");
        }
        var enchantmentId = data.getEnchantment();
        if (enchantmentId == null) {
            throw new IllegalArgumentException("Mystical Agriculture enchanter enchantment cannot be empty");
        }
        var enchantmentKey = ResourceKey.create(Registries.ENCHANTMENT, enchantmentId);
        var enchantment = Platform.getFrozenRegistry()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .get(enchantmentKey)
                .orElseThrow(() -> new IllegalArgumentException("Unknown enchantment: " + enchantmentId));
        return new EnchanterRecipe(ingredients, enchantment);
    }

    public static Recipe<?> compileReprocessor(MysticalAgricultureReprocessorRecipeData data) {
        return new ReprocessorRecipe(
                requireIngredient(data.getInput(), "Mystical Agriculture reprocessor input cannot be empty"),
                requireItem(data.getResult(), "Mystical Agriculture reprocessor result cannot be empty")
        );
    }

    public static Recipe<?> compileSoulExtraction(MysticalAgricultureSoulExtractionRecipeData data) {
        var soulTypeId = data.getSoulType();
        if (soulTypeId == null || MobSoulTypeRegistry.getInstance().getMobSoulTypeById(soulTypeId) == null) {
            throw new IllegalArgumentException("Unknown Mystical Agriculture mob soul type: " + soulTypeId);
        }
        if (!Double.isFinite(data.getSouls()) || data.getSouls() <= 0) {
            throw new IllegalArgumentException("Mystical Agriculture extracted souls must be greater than zero");
        }
        return new SoulExtractionRecipe(
                requireIngredient(data.getInput(), "Mystical Agriculture soul extraction input cannot be empty"),
                new SoulExtractionRecipe.Result(soulTypeId, data.getSouls())
        );
    }

    public static Recipe<?> compileSouliumSpawner(MysticalAgricultureSouliumSpawnerRecipeData data) {
        var weightedEntities = new ArrayList<WeightedEntry.Wrapper<net.minecraft.world.entity.EntityType<?>>>();
        for (var entityData : data.getEntities()) {
            if (entityData == null || entityData.getEntity() == null) {
                continue;
            }
            var entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityData.getEntity())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown entity type: " + entityData.getEntity()));
            weightedEntities.add(WeightedEntry.wrap(entityType, Math.max(1, entityData.getWeight())));
        }
        if (weightedEntities.isEmpty()) {
            throw new IllegalArgumentException("Mystical Agriculture Soulium spawner recipe must have at least one entity");
        }
        return new SouliumSpawnerRecipe(
                compileCountedIngredient(data.getInput()),
                WeightedRandomList.create(weightedEntities)
        );
    }

    private static IngredientWithCount compileCountedIngredient(RecipeIngredient data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Mystical Agriculture counted ingredient cannot be empty");
        }
        var values = new ArrayList<Ingredient.Value>();
        var kind = data.getKind() == null ? IngredientValueKind.ITEM : data.getKind();
        if (kind == IngredientValueKind.ITEM) {
            var item = normalizeItem(data.getItem());
            if (!item.isEmpty()) {
                values.add(new Ingredient.ItemValue(item.copyWithCount(1)));
            }
        } else if (kind == IngredientValueKind.TAG && data.getTag() != null) {
            values.add(new Ingredient.TagValue(TagKey.create(Registries.ITEM, data.getTag())));
        } else if (kind == IngredientValueKind.ITEM_ABILITY) {
            throw new IllegalArgumentException("Mystical Agriculture counted ingredients do not support item abilities");
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Mystical Agriculture counted ingredient cannot be empty");
        }
        return new IngredientWithCount(values.toArray(Ingredient.Value[]::new), Math.max(1, data.getCount()));
    }

    private static Ingredient requireIngredient(RecipeIngredient data, String message) {
        var ingredient = compileIngredient(data);
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return ingredient;
    }

    private static Ingredient compileIngredient(RecipeIngredient data) {
        return data == null ? Ingredient.EMPTY : data.compile();
    }

    private static ItemStack requireItem(ItemStack stack, String message) {
        var item = normalizeItem(stack);
        if (item.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return item;
    }

    private static ItemStack normalizeItem(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            return ItemStack.EMPTY;
        }
        var copy = stack.copy();
        copy.setCount(Math.max(1, copy.getCount()));
        return copy;
    }
}
