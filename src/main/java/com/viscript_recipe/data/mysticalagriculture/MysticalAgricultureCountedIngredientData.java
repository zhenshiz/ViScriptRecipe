package com.viscript_recipe.data.mysticalagriculture;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
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
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.STONE);
    @Persisted
    private int count = 1;
}
