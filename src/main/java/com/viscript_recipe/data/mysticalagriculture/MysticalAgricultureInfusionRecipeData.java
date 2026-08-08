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
public class MysticalAgricultureInfusionRecipeData implements IVSRecipeData {
    public static final int MAX_PEDESTAL_INGREDIENTS = 8;

    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.DIAMOND);
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.EMERALD);
    @Persisted
    private boolean transferComponents;

    public RecipeIngredient ingredient(int index) {
        return index >= 0 && index < ingredients.size() ? ingredients.get(index) : RecipeIngredient.empty();
    }

    public MysticalAgricultureInfusionRecipeData setIngredient(int index, RecipeIngredient ingredient) {
        while (ingredients.size() <= index && ingredients.size() < MAX_PEDESTAL_INGREDIENTS) {
            ingredients.add(RecipeIngredient.empty());
        }
        if (index >= 0 && index < ingredients.size()) {
            ingredients.set(index, ingredient == null ? RecipeIngredient.empty() : ingredient);
        }
        return this;
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return MysticalAgricultureRecipeFactory.compileInfusion(this);
    }
}
