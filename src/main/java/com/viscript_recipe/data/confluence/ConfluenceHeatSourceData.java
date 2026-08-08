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
public class ConfluenceHeatSourceData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.confluence.heat_source.blocks", subConfigurable = true)
    private ConfluenceHolderSetData blocks = new ConfluenceHolderSetData();

    @Configurable(name = "viscript_recipe.config.confluence.heat_source.has_state")
    private boolean hasState;

    @Configurable(name = "viscript_recipe.config.confluence.heat_source.state", subConfigurable = true)
    private ConfluenceStatePredicateData state = new ConfluenceStatePredicateData();

    @Configurable(name = "viscript_recipe.config.confluence.heat_source.has_nbt")
    private boolean hasNbt;

    @Configurable(name = "viscript_recipe.config.confluence.heat_source.nbt")
    private String nbt = "{}";
}
