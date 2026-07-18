package com.viscript_recipe.data.mysticalagriculture;

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

@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureReprocessorRecipeData implements IVSRecipeData {
    @Configurable(name = "viscript_recipe.config.mysticalagriculture.reprocessor.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.WHEAT_SEEDS);

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.result")
    private ItemStack result = new ItemStack(Items.WHEAT);

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return MysticalAgricultureRecipeFactory.compileReprocessor(this);
    }
}
