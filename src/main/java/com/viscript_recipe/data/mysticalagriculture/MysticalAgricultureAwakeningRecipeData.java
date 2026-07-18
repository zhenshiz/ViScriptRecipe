package com.viscript_recipe.data.mysticalagriculture;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
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

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.awakening.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.NETHER_STAR);

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.awakening.ingredients", subConfigurable = true)
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.awakening.essences")
    private List<ItemStack> essences = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.result")
    private ItemStack result = new ItemStack(Items.NETHER_STAR);

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.transfer_components")
    private boolean transferComponents;

    public RecipeIngredient createDefaultIngredient() {
        return RecipeIngredient.item(Items.STONE);
    }

    public RecipeIngredient ingredient(int index) {
        return index >= 0 && index < ingredients.size() ? ingredients.get(index) : new RecipeIngredient();
    }

    public MysticalAgricultureAwakeningRecipeData setIngredient(int index, RecipeIngredient ingredient) {
        while (ingredients.size() <= index && ingredients.size() < PEDESTAL_INGREDIENT_COUNT) {
            ingredients.add(new RecipeIngredient());
        }
        if (index >= 0 && index < ingredients.size()) {
            ingredients.set(index, ingredient == null ? new RecipeIngredient() : ingredient);
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
