package com.viscript_recipe.data.mekanism;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum MekanismChemicalIngredientKind implements StringRepresentable {
    CHEMICAL("chemical"),
    TAG("tag");

    private final String serializedName;

    MekanismChemicalIngredientKind(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }
}
