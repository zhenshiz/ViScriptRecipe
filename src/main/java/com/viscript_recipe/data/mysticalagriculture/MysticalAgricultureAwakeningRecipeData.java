package com.viscript_recipe.data.mysticalagriculture;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureAwakeningRecipeData implements IVSRecipeData {
    public static final int PEDESTAL_INGREDIENT_COUNT = 4;
    public static final int ESSENCE_COUNT = 4;

    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.NETHER_STAR);
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @Persisted
    private List<ItemStack> essences = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.NETHER_STAR);
    @Persisted
    private boolean transferComponents;

    public RecipeIngredient ingredient(int index) {
        return index >= 0 && index < ingredients.size() ? ingredients.get(index) : RecipeIngredient.empty();
    }

    public MysticalAgricultureAwakeningRecipeData setIngredient(int index, RecipeIngredient ingredient) {
        while (ingredients.size() <= index && ingredients.size() < PEDESTAL_INGREDIENT_COUNT) {
            ingredients.add(RecipeIngredient.empty());
        }
        if (index >= 0 && index < ingredients.size()) {
            ingredients.set(index, ingredient == null ? RecipeIngredient.empty() : ingredient);
        }
        return this;
    }

    public ItemStack essence(int index) {
        return index >= 0 && index < essences.size() && essences.get(index) != null
                ? essences.get(index)
                : ItemStack.EMPTY;
    }

    public MysticalAgricultureAwakeningRecipeData setEssence(int index, ItemStack essence) {
        while (essences.size() <= index && essences.size() < ESSENCE_COUNT) {
            essences.add(ItemStack.EMPTY);
        }
        if (index >= 0 && index < essences.size()) {
            essences.set(index, essence == null ? ItemStack.EMPTY : essence.copy());
        }
        return this;
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return MysticalAgricultureRecipeFactory.compileAwakening(this);
    }
}
