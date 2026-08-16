package com.viscript_recipe.compat.industrial_foregoing.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

@Getter
@Setter
@Accessors(chain = true)
public class IndustrialEntityConditionData implements ISkipDefaultedSerialize, IConfigurable {
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
