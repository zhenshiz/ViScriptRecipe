package com.viscript_recipe.data.create;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum CreateHeatCondition implements StringRepresentable {
    NONE("none"),
    HEATED("heated"),
    SUPERHEATED("superheated");

    @Getter
    private final String serializedName;

    CreateHeatCondition(String serializedName) {
        this.serializedName = serializedName;
    }

    public Component displayName() {
        return Component.translatable("viscript_recipe.editor.create.heat." + serializedName);
    }
}
