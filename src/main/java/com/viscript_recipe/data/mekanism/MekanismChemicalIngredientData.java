package com.viscript_recipe.data.mekanism;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

@Getter
@Setter
@Accessors(chain = true)
public class MekanismChemicalIngredientData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.mekanism.chemical_ingredient.kind")
    @ConfigSelector(candidate = {"chemical", "tag"})
    private MekanismChemicalIngredientKind kind = MekanismChemicalIngredientKind.CHEMICAL;

    @Configurable(name = "viscript_recipe.config.mekanism.chemical_ingredient.chemical")
    private ResourceLocation chemical = ResourceLocation.fromNamespaceAndPath("mekanism", "oxygen");

    @Configurable(name = "viscript_recipe.config.mekanism.chemical_ingredient.tag")
    private ResourceLocation tag = ResourceLocation.fromNamespaceAndPath("mekanism", "clean");

    @Configurable(name = "viscript_recipe.config.mekanism.chemical_ingredient.amount")
    private long amount = 1;
}
