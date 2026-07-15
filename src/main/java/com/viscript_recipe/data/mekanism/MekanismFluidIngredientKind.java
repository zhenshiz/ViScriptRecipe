package com.viscript_recipe.data.mekanism;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum MekanismFluidIngredientKind implements StringRepresentable {
    FLUID("fluid"),
    TAG("tag");

    private final String serializedName;

    MekanismFluidIngredientKind(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }
}
