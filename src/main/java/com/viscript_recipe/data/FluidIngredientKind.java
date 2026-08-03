package com.viscript_recipe.data;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum FluidIngredientKind implements StringRepresentable {
    FLUID("fluid"),
    TAG("tag");

    @Getter
    private final String serializedName;

    FluidIngredientKind(String serializedName) {
        this.serializedName = serializedName;
    }

    public Component displayName() {
        return Component.translatable("viscript_recipe.editor.fluid_ingredient.kind." + serializedName);
    }
}
