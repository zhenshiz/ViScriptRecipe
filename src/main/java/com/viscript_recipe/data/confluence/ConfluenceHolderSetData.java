package com.viscript_recipe.data.confluence;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ConfluenceHolderSetData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.confluence.holder_set.kind")
    @ConfigSelector(candidate = {"none", "ids", "tag"})
    private ConfluenceHolderSetKind kind = ConfluenceHolderSetKind.NONE;

    @Configurable(name = "viscript_recipe.config.confluence.holder_set.tag")
    private ResourceLocation tag = ResourceLocation.withDefaultNamespace("empty");

    @Configurable(name = "viscript_recipe.config.confluence.holder_set.values")
    @ConfigList(addDefaultMethod = "createDefaultValue")
    private List<ResourceLocation> values = new ArrayList<>();

    public ResourceLocation createDefaultValue() {
        return ResourceLocation.withDefaultNamespace("air");
    }
}
