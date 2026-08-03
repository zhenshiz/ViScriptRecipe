package com.viscript_recipe.data.mekanism;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

@Getter
@Setter
@Accessors(chain = true)
public class MekanismChemicalIngredientData implements IPersistedSerializable {
    @Persisted
    private MekanismChemicalIngredientKind kind = MekanismChemicalIngredientKind.CHEMICAL;
    @Persisted
    private ResourceLocation chemical = ResourceLocation.fromNamespaceAndPath("mekanism", "oxygen");
    @Persisted
    private ResourceLocation tag = ResourceLocation.fromNamespaceAndPath("mekanism", "clean");
    @Persisted
    private long amount = 1;
}
