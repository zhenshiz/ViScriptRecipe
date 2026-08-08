package com.viscript_recipe.data.mekanism;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum MekanismChemicalIngredientKind implements StringRepresentable {
    CHEMICAL("chemical"),
    TAG("tag");

    @Getter
    private final String serializedName;

    MekanismChemicalIngredientKind(String serializedName) {
        this.serializedName = serializedName;
    }

    public Component displayName() {
        return Component.translatable("viscript_recipe.editor.mekanism.chemical_kind." + serializedName);
    }
}
