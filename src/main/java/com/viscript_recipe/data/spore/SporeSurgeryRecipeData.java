package com.viscript_recipe.data.spore;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.spore.SporeRecipeFactory;
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
public class SporeSurgeryRecipeData implements IVSRecipeData {
    public static final int INPUT_COUNT = 16;

    @Persisted
    private List<RecipeIngredient> ingredients = emptyIngredients(INPUT_COUNT);
    @Persisted
    private ItemStack result = new ItemStack(Items.IRON_SWORD);

    public RecipeIngredient ingredient(int index) {
        if (ingredients == null || index < 0 || index >= ingredients.size()) {
            return RecipeIngredient.empty();
        }
        var ingredient = ingredients.get(index);
        return ingredient == null ? RecipeIngredient.empty() : ingredient;
    }

    public SporeSurgeryRecipeData setIngredient(int index, RecipeIngredient ingredient) {
        ingredients = normalizedIngredients(ingredients, INPUT_COUNT);
        if (index >= 0 && index < INPUT_COUNT) {
            ingredients.set(index, ingredient == null ? RecipeIngredient.empty() : ingredient);
        }
        return this;
    }

    public List<RecipeIngredient> normalizedIngredients() {
        return normalizedIngredients(ingredients, INPUT_COUNT);
    }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return SporeRecipeFactory.compileSurgery(this);
    }

    static List<RecipeIngredient> normalizedIngredients(List<RecipeIngredient> source, int size) {
        var normalized = emptyIngredients(size);
        if (source != null) {
            for (int i = 0; i < Math.min(size, source.size()); i++) {
                var ingredient = source.get(i);
                normalized.set(i, ingredient == null ? RecipeIngredient.empty() : ingredient);
            }
        }
        return normalized;
    }

    private static List<RecipeIngredient> emptyIngredients(int size) {
        var ingredients = new ArrayList<RecipeIngredient>(size);
        for (int i = 0; i < size; i++) {
            ingredients.add(RecipeIngredient.empty());
        }
        return ingredients;
    }
}
