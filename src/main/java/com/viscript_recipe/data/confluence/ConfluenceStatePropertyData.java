package com.viscript_recipe.data.confluence;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ConfluenceStatePropertyData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.confluence.state_property.name")
    private String name = "lit";

    @Configurable(name = "viscript_recipe.config.confluence.state_property.ranged")
    private boolean ranged;

    @Configurable(name = "viscript_recipe.config.confluence.state_property.value")
    private String value = "true";

    @Configurable(name = "viscript_recipe.config.confluence.state_property.min")
    private String min = "";

    @Configurable(name = "viscript_recipe.config.confluence.state_property.max")
    private String max = "";
}
