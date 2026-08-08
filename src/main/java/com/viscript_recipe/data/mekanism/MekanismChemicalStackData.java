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
public class MekanismChemicalStackData implements IPersistedSerializable {
    @Persisted
    private ResourceLocation chemical = ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen");
    @Persisted
    private long amount = 1;

    public boolean isEmpty() {
        return chemical == null || amount <= 0;
    }
}
