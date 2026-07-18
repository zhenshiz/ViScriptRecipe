package com.viscript_recipe.data.industrial_foregoing;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

@Getter
@Setter
@Accessors(chain = true)
public class IndustrialEntityConditionData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.industrial_foregoing.entity_condition.enabled")
    private boolean enabled;

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.entity_condition.kind")
    @ConfigSelector(candidate = {"entity", "tag"})
    private IndustrialEntityIngredientKind kind = IndustrialEntityIngredientKind.ENTITY;

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.entity_condition.id")
    private ResourceLocation id = ResourceLocation.withDefaultNamespace("wither");

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.entity_condition.nbt")
    private String nbt = "{}";

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.entity_condition.display")
    private String display = "";
}
