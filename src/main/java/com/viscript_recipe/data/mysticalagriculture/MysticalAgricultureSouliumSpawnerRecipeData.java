package com.viscript_recipe.data.mysticalagriculture;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeFactory;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the counted input and weighted entity results of a Soulium spawner recipe.
 */
@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureSouliumSpawnerRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.mysticalagriculture.soulium_spawner.input", subConfigurable = true)
    private MysticalAgricultureCountedIngredientData input = new MysticalAgricultureCountedIngredientData()
            .setIngredient(RecipeIngredient.item(Items.ROTTEN_FLESH));

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.soulium_spawner.entities", subConfigurable = true)
    @ConfigList(addDefaultMethod = "createDefaultEntity")
    private List<MysticalAgricultureWeightedEntityData> entities = new ArrayList<>();

    public MysticalAgricultureWeightedEntityData createDefaultEntity() {
        return new MysticalAgricultureWeightedEntityData();
    }

    public Recipe<?> compile() {
        return MysticalAgricultureRecipeFactory.compileSouliumSpawner(this);
    }
}
