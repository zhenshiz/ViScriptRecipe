package com.viscript_recipe.compat.mysticalagriculture.data;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

/** 实体的刷怪选择权重必须为正数。 */
@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureWeightedEntityData implements IPersistedSerializable {
    @Persisted
    private ResourceLocation entity = ResourceLocation.withDefaultNamespace("zombie");
    @Persisted
    private int weight = 1;
}
