package com.viscript_recipe.data.extendedcrafting;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.Items;

@Getter
@Setter
@Accessors(chain = true)
public class ExtendedCraftingCountedIngredientData implements IPersistedSerializable, IConfigurable {
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.COBBLESTONE);
    @Persisted
    private int count = 1;
}
