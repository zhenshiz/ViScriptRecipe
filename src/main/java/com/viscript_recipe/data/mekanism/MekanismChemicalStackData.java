package com.viscript_recipe.data.mekanism;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

@Getter
@Setter
@Accessors(chain = true)
public class MekanismChemicalStackData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.mekanism.chemical_output.chemical")
    private ResourceLocation chemical = ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen");

    @Configurable(name = "viscript_recipe.config.mekanism.chemical_output.amount")
    private long amount = 1;

    public boolean isEmpty() {
        return chemical == null || amount <= 0;
    }
}
