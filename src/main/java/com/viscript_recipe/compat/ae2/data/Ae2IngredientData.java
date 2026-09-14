package com.viscript_recipe.compat.ae2.data;

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

/** Stores every alternative of an AE2 ingredient, including client-expanded tag contents. */
@Getter
@Setter
@Accessors(chain = true)
public class Ae2IngredientData implements ISkipDefaultedSerialize {
    @Persisted
    private List<RecipeIngredient> alternatives = new ArrayList<>();

    public static Ae2IngredientData of(RecipeIngredient ingredient) {
        var data = new Ae2IngredientData();
        if (!ingredient.isEmpty()) data.alternatives.add(ingredient);
        return data;
    }

    public boolean isEmpty() { return alternatives.stream().allMatch(RecipeIngredient::isEmpty); }

    public Ingredient compile() {
        var children = alternatives.stream().filter(value -> !value.isEmpty()).map(RecipeIngredient::compile).toList();
        if (children.stream().anyMatch(Ingredient::isCustom)) {
            return CompoundIngredient.of(children.toArray(Ingredient[]::new));
        }
        return Ingredient.fromValues(children.stream().flatMap(value -> Arrays.stream(value.getValues())));
    }
}
