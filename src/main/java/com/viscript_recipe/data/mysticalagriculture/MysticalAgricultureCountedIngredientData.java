package com.viscript_recipe.data.mysticalagriculture;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.Items;

/**
 * Stores a Mystical Agriculture ingredient together with its required stack count.
 */
@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureCountedIngredientData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.mysticalagriculture.counted_ingredient.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.STONE);

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.counted_ingredient.count")
    private int count = 1;
}
