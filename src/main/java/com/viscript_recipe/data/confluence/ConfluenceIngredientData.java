package com.viscript_recipe.data.confluence;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.Items;

@Getter
@Setter
@Accessors(chain = true)
public class ConfluenceIngredientData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.confluence.ingredient.value", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.STONE);

    @Configurable(name = "viscript_recipe.config.confluence.ingredient.count")
    private int count = 1;
}
