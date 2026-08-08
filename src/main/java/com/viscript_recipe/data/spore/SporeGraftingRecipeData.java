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

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class SporeGraftingRecipeData implements IVSRecipeData {
    public static final int INPUT_COUNT = 3;

    @Persisted
    private List<RecipeIngredient> ingredients = SporeSurgeryRecipeData.normalizedIngredients(null, INPUT_COUNT);
    @Persisted
    private ItemStack result = new ItemStack(Items.IRON_HELMET);

    public RecipeIngredient ingredient(int index) {
        if (ingredients == null || index < 0 || index >= ingredients.size()) {
            return RecipeIngredient.empty();
        }
        var ingredient = ingredients.get(index);
        return ingredient == null ? RecipeIngredient.empty() : ingredient;
    }

    public SporeGraftingRecipeData setIngredient(int index, RecipeIngredient ingredient) {
        ingredients = normalizedIngredients();
        if (index >= 0 && index < INPUT_COUNT) {
            ingredients.set(index, ingredient == null ? RecipeIngredient.empty() : ingredient);
        }
        return this;
    }

    public List<RecipeIngredient> normalizedIngredients() {
        return SporeSurgeryRecipeData.normalizedIngredients(ingredients, INPUT_COUNT);
    }

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return SporeRecipeFactory.compileGrafting(this);
    }
}
