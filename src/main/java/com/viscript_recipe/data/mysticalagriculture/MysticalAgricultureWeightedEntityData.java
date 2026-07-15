package com.viscript_recipe.data.mysticalagriculture;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

/**
 * Stores an entity registry identifier and its positive spawner selection weight.
 */
@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureWeightedEntityData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.mysticalagriculture.soulium_spawner.entity")
    private ResourceLocation entity = ResourceLocation.withDefaultNamespace("zombie");

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.soulium_spawner.weight")
    private int weight = 1;
}
