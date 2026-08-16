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
public class ConfluenceStatePropertyData implements ISkipDefaultedSerialize, IConfigurable {
    @Persisted
    private String name = "lit";
    @Persisted
    private boolean ranged;
    @Persisted
    private String value = "true";
    @Persisted
    private String min = "";
    @Persisted
    private String max = "";
}
