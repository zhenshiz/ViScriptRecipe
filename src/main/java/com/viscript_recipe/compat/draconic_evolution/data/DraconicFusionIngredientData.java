package com.viscript_recipe.compat.draconic_evolution.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class DraconicFusionIngredientData implements ISkipDefaultedSerialize {
    @Persisted
    private DraconicIngredientData ingredient = new DraconicIngredientData();
    @Persisted
    private boolean consume = true;
}
