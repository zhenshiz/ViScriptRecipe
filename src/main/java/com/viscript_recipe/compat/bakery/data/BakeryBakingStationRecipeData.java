package com.viscript_recipe.compat.bakery.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.farm_and_charm.data.FarmCharmIngredientData;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.satisfy.bakery.core.recipe.BakingStationRecipe;
import net.satisfy.bakery.core.registry.ObjectRegistry;

import java.util.ArrayList;
import java.util.List;

/** 保留烘焙材料的全部候选项，使用原生配方和接口默认值初始化。 */
@Getter
@Setter
@Accessors(chain = true)
public final class BakeryBakingStationRecipeData implements IVSRecipeData {
    public static final int MAX_INPUTS = 3;
    @Persisted private List<FarmCharmIngredientData> inputs = new ArrayList<>();
    @Persisted private ItemStack result = new ItemStack(Items.CAKE);
    @Persisted private int inputSlots = 2;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        var ingredients = NonNullList.<Ingredient>create();
        for (var input : inputs) if (!input.isEmpty()) ingredients.add(input.compile());
        if (ingredients.isEmpty() || ingredients.size() > MAX_INPUTS)
            throw new IllegalArgumentException("Bakery baking station requires 1 to 3 ingredients");
        if (result.isEmpty()) throw new IllegalArgumentException("Bakery baking station requires a result item");
        return new BakingStationRecipe(ingredients, result.copy());
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        inputs = new ArrayList<>(List.of(
                FarmCharmIngredientData.of(RecipeIngredient.item(ObjectRegistry.CAKE_DOUGH.get())),
                FarmCharmIngredientData.of(RecipeIngredient.item(Items.SUGAR))));
        result = new ItemStack(Items.CAKE);
        inputSlots = 2;
    }
}
