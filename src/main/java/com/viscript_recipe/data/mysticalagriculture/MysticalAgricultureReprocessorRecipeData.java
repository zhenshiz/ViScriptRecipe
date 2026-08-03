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

@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureReprocessorRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.WHEAT_SEEDS);
    @Persisted
    private ItemStack result = new ItemStack(Items.WHEAT);

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return MysticalAgricultureRecipeFactory.compileReprocessor(this);
    }
}
