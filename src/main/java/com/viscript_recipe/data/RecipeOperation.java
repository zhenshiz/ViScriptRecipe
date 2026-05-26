package com.viscript_recipe.data;

import net.minecraft.util.StringRepresentable;

public enum RecipeOperation implements StringRepresentable {
    ADD("add"),
    REPLACE("replace"),
    REMOVE("remove");

    private final String serializedName;

    RecipeOperation(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
