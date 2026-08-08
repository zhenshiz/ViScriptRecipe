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
public class ConfluenceStatePredicateData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.confluence.state_predicate.properties")
    @ConfigList(addDefaultMethod = "createDefaultProperty")
    private List<ConfluenceStatePropertyData> properties = new ArrayList<>();

    public ConfluenceStatePropertyData createDefaultProperty() {
        return new ConfluenceStatePropertyData();
    }
}
