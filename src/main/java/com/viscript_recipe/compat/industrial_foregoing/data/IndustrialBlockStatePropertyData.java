package com.viscript_recipe.compat.industrial_foregoing.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class IndustrialBlockStatePropertyData implements ISkipDefaultedSerialize, IConfigurable {
    @Persisted
    private String name = "";
    @Persisted
    private String value = "";
}
