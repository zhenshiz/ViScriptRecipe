package com.viscript_recipe.data.alloy_smelter;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/** Stores one Alloy Smelter ingredient and the independently required amount. */
@Getter
@Setter
@Accessors(chain = true)
public class AlloySmelterMaterialData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.alloy_smelter.material.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = new RecipeIngredient();

    @Configurable(name = "viscript_recipe.config.alloy_smelter.material.count")
    private int count = 1;
}
