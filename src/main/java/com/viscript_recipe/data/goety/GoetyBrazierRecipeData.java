package com.viscript_recipe.data.goety;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.goety.GoetyRecipeFactory;
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
public class GoetyBrazierRecipeData implements IVSRecipeData {
    public static final int INPUT_COUNT = 3;

    @Persisted
    private List<RecipeIngredient> ingredients = emptyIngredients();
    @Persisted
    private ItemStack result = new ItemStack(Items.IRON_INGOT);
    @Persisted
    private int soulCost = 500;

    public RecipeIngredient ingredient(int index) {
        if (ingredients == null || index < 0 || index >= ingredients.size()) {
            return RecipeIngredient.empty();
        }
        var ingredient = ingredients.get(index);
        return ingredient == null ? RecipeIngredient.empty() : ingredient;
    }

    public GoetyBrazierRecipeData setIngredient(int index, RecipeIngredient ingredient) {
        ingredients = normalizedIngredients();
        if (index >= 0 && index < INPUT_COUNT) {
            ingredients.set(index, ingredient == null ? RecipeIngredient.empty() : ingredient);
        }
        return this;
    }

    public List<RecipeIngredient> normalizedIngredients() {
        var normalized = emptyIngredients();
        if (ingredients != null) {
            for (int i = 0; i < Math.min(INPUT_COUNT, ingredients.size()); i++) {
                var ingredient = ingredients.get(i);
                normalized.set(i, ingredient == null ? RecipeIngredient.empty() : ingredient);
            }
        }
        return normalized;
    }

    private static List<RecipeIngredient> emptyIngredients() {
        var result = new ArrayList<RecipeIngredient>(INPUT_COUNT);
        for (int i = 0; i < INPUT_COUNT; i++) {
            result.add(RecipeIngredient.empty());
        }
        return result;
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return GoetyRecipeFactory.compileBrazier(this);
    }
}
