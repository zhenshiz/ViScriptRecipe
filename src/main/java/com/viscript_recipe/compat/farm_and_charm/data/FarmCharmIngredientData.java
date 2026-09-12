package com.viscript_recipe.compat.farm_and_charm.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Preserves every accepted item or tag of one input, including client-expanded alternatives. */
@Getter
@Setter
@Accessors(chain = true)
public class FarmCharmIngredientData implements ISkipDefaultedSerialize {
    @Persisted
    private List<RecipeIngredient> alternatives = new ArrayList<>();

    public static FarmCharmIngredientData of(RecipeIngredient ingredient) {
        var data = new FarmCharmIngredientData();
        if (!ingredient.isEmpty()) data.alternatives.add(ingredient);
        return data;
    }

    public boolean isEmpty() { return alternatives.stream().allMatch(RecipeIngredient::isEmpty); }

    /**
     * Combines all nonempty alternatives into one accepted ingredient.
     *
     * @return the union of the alternatives, or an empty ingredient if all alternatives are empty
     */
    public Ingredient compile() {
        var children = alternatives.stream().filter(value -> !value.isEmpty()).map(RecipeIngredient::compile).toList();
        if (children.stream().anyMatch(Ingredient::isCustom)) {
            return CompoundIngredient.of(children.toArray(Ingredient[]::new));
        }
        return Ingredient.fromValues(children.stream().flatMap(value -> Arrays.stream(value.getValues())));
    }
}
