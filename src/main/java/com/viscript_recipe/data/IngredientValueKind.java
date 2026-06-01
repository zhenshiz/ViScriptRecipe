package com.viscript_recipe.data;

import net.minecraft.util.StringRepresentable;

public enum IngredientValueKind implements StringRepresentable {
    ITEM("item"),
    TAG("tag"),
    ITEM_ABILITY("item_ability");

    private final String serializedName;

    IngredientValueKind(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
