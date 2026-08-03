package com.viscript_recipe.data.industrial_foregoing;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

@Getter
@Setter
@Accessors(chain = true)
public class IndustrialEntityConditionData implements IPersistedSerializable, IConfigurable {
    @Persisted
    private boolean enabled;
    @Persisted
    private IndustrialEntityIngredientKind kind = IndustrialEntityIngredientKind.ENTITY;
    @Persisted
    private ResourceLocation id = ResourceLocation.withDefaultNamespace("wither");
    @Persisted
    private String nbt = "{}";
    @Persisted
    private String display = "";
}
