package com.viscript_recipe.compat.eidolon.data;

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

@Getter
@Setter
@Accessors(chain = true)
public class EidolonIngredientData implements ISkipDefaultedSerialize {
    @Persisted
    private List<EidolonIngredientValueData> alternatives = new ArrayList<>();

    public static EidolonIngredientData of(RecipeIngredient ingredient) {
        var data = new EidolonIngredientData();
        if (!ingredient.isEmpty()) data.alternatives.add(EidolonIngredientValueData.of(ingredient));
        return data;
    }

    public boolean isEmpty() { return alternatives.stream().allMatch(input -> input.getValue().isEmpty()); }

    public Ingredient compile() {
        var children = alternatives.stream().filter(value -> !value.getValue().isEmpty()).map(EidolonIngredientValueData::compile).toList();
        if (children.isEmpty()) return Ingredient.EMPTY;
        if (children.size() == 1) return children.getFirst();
        if (children.stream().anyMatch(Ingredient::isCustom)) return CompoundIngredient.of(children.toArray(Ingredient[]::new));
        return Ingredient.fromValues(children.stream().flatMap(value -> Arrays.stream(value.getValues())));
    }
}
