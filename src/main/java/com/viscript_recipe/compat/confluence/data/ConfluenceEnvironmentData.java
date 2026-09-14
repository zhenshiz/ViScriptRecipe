package com.viscript_recipe.compat.confluence.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Accessors(chain = true)
public class ConfluenceEnvironmentData implements IPersistedSerializable, IConfigurable {
    @Persisted
    private ConfluenceHolderSetData biomes = new ConfluenceHolderSetData();
    @Persisted
    private int inflate = 1;
    @Persisted
    private ConfluenceHolderSetData blocks = new ConfluenceHolderSetData();
    @Persisted
    private List<ConfluenceStatePredicateData> statePredicates = new ArrayList<>();
    @Persisted
    private ConfluenceHolderSetData fluids = new ConfluenceHolderSetData();
    @Persisted
    private boolean graveyard;

    public String statePredicatesText() {
        if (statePredicates.isEmpty()) return "";
        return statePredicates.stream()
                .filter(java.util.Objects::nonNull)
                .map(predicate -> predicate.getProperties().stream()
                        .filter(java.util.Objects::nonNull)
                        .map(property -> property.isRanged()
                                         ? property.getName() + "=" + property.getMin() + ".." + property.getMax()
                                         : property.getName() + "=" + property.getValue())
                        .collect(Collectors.joining(",")))
                .filter(text -> !text.isBlank())
                .collect(Collectors.joining(";"));
    }
}
