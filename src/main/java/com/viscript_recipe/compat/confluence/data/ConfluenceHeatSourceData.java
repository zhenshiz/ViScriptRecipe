package com.viscript_recipe.compat.confluence.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ConfluenceHeatSourceData implements ISkipDefaultedSerialize, IConfigurable {
    @Persisted
    private ConfluenceHolderSetData blocks = new ConfluenceHolderSetData();
    @Persisted
    private boolean hasState;
    @Persisted
    private ConfluenceStatePredicateData state = new ConfluenceStatePredicateData();
    @Persisted
    private boolean hasNbt;
    @Persisted
    private String nbt = "{}";
}
