package com.viscript_recipe.compat.mysticalagriculture.data;

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

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

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

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setInput(RecipeIngredient.item(itemFromRegistry("mysticalagriculture:inferium_seeds", Items.WHEAT_SEEDS)))
                .setResult(new ItemStack(itemFromRegistry("mysticalagriculture:inferium_essence", Items.WHEAT)));
    }
}
