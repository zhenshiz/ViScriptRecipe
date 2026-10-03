package com.viscript_recipe.compat.draconic_evolution.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/** Keeps all alternatives and the native StackIngredient wrapper, including a count of one. */
@Getter
@Setter
@Accessors(chain = true)
public class DraconicIngredientData implements ISkipDefaultedSerialize {
    @Persisted
    private List<RecipeIngredient> alternatives = new ArrayList<>();
    @Persisted
    private int count = 1;
    @Persisted
    private boolean stackIngredient;

    public static DraconicIngredientData of(RecipeIngredient ingredient) {
        var data = new DraconicIngredientData();
        if (!ingredient.isEmpty()) data.alternatives.add(ingredient.copy());
        return data;
    }

    public boolean isEmpty() { return alternatives.stream().allMatch(RecipeIngredient::isEmpty); }
}
