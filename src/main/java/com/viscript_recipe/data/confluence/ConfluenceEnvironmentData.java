package com.viscript_recipe.data.confluence;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ConfluenceEnvironmentData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.confluence.environment.biomes", subConfigurable = true)
    private ConfluenceHolderSetData biomes = new ConfluenceHolderSetData();

    @Configurable(name = "viscript_recipe.config.confluence.environment.inflate")
    private int inflate = 1;

    @Configurable(name = "viscript_recipe.config.confluence.environment.blocks", subConfigurable = true)
    private ConfluenceHolderSetData blocks = new ConfluenceHolderSetData();

    @Configurable(name = "viscript_recipe.config.confluence.environment.state_predicates")
    @ConfigList(addDefaultMethod = "createDefaultStatePredicate")
    private List<ConfluenceStatePredicateData> statePredicates = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.confluence.environment.fluids", subConfigurable = true)
    private ConfluenceHolderSetData fluids = new ConfluenceHolderSetData();

    @Configurable(name = "viscript_recipe.config.confluence.environment.graveyard")
    private boolean graveyard;

    public ConfluenceStatePredicateData createDefaultStatePredicate() {
        return new ConfluenceStatePredicateData();
    }
}
