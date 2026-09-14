package com.viscript_recipe.compat.confluence.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ConfluenceHolderSetData implements ISkipDefaultedSerialize, IConfigurable {
    @Persisted
    private ConfluenceHolderSetKind kind = ConfluenceHolderSetKind.NONE;
    @Persisted
    private ResourceLocation tag = ResourceLocation.withDefaultNamespace("empty");
    @Persisted
    private List<ResourceLocation> values = new ArrayList<>(List.of(ResourceLocation.withDefaultNamespace("campfire")));

    public static ConfluenceHolderSetData tag(ResourceLocation tag) {
        return new ConfluenceHolderSetData().setKind(ConfluenceHolderSetKind.TAG).setTag(tag);
    }

    public static ConfluenceHolderSetData ids(ResourceLocation id) {
        return new ConfluenceHolderSetData().setKind(ConfluenceHolderSetKind.IDS).setValues(new ArrayList<>(List.of(id)));
    }

    public ResourceLocation firstId() {
        return values.isEmpty() ? ResourceLocation.parse("") : values.getFirst();
    }

    public RecipeIngredient asVisualIngredient() {
        return switch (kind) {
            case TAG -> RecipeIngredient.tag(tag);
            case IDS -> RecipeIngredient.item(BuiltInRegistries.BLOCK.get(firstId()).asItem());
            default -> RecipeIngredient.empty();
        };
    }
}
