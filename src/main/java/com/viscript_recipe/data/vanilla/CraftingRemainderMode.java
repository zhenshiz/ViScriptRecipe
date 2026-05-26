package com.viscript_recipe.data.vanilla;

import net.minecraft.util.StringRepresentable;

public enum CraftingRemainderMode implements StringRepresentable {
    DEFAULT("default"),
    CONSUME("consume"),
    REPLACE("replace");

    private final String serializedName;

    CraftingRemainderMode(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
