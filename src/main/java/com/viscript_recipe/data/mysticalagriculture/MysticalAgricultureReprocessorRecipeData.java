package com.viscript_recipe.data.mysticalagriculture;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeFactory;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Stores the single input and output encoded by a reprocessor recipe.
 */
@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureReprocessorRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.mysticalagriculture.reprocessor.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.WHEAT_SEEDS);

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.result")
    private ItemStack result = new ItemStack(Items.WHEAT);

    public Recipe<?> compile() {
        return MysticalAgricultureRecipeFactory.compileReprocessor(this);
    }
}
