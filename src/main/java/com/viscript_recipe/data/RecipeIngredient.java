package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class RecipeIngredient implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.ingredient.values")
    @ConfigList(addDefaultMethod = "createDefaultValue")
    private List<RecipeIngredientValue> values = new ArrayList<>();

    public static RecipeIngredient item(Item item) {
        var ingredient = new RecipeIngredient();
        ingredient.values.add(RecipeIngredientValue.item(item));
        return ingredient;
    }

    public RecipeIngredientValue createDefaultValue() {
        return new RecipeIngredientValue();
    }

    public Ingredient compile() {
        if (values.isEmpty()) {
            return Ingredient.EMPTY;
        }
        return Ingredient.fromValues(values.stream().map(RecipeIngredientValue::compile));
    }
}
