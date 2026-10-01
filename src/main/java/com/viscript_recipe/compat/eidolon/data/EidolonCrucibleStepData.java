package com.viscript_recipe.compat.eidolon.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class EidolonCrucibleStepData implements ISkipDefaultedSerialize {
    @Persisted
    private int stirs;
    @Persisted
    private List<EidolonIngredientData> items = EidolonWorktableRecipeData.emptySlots(4);
}
