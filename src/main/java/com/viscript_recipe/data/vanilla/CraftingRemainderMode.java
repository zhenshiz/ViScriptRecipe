package com.viscript_recipe.data.vanilla;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum CraftingRemainderMode implements StringRepresentable {
    DEFAULT("default"),
    CONSUME("consume"),
    REPLACE("replace");

    @Getter
    private final String serializedName;

    CraftingRemainderMode(String serializedName) {
        this.serializedName = serializedName;
    }

    public Component displayName() {
        return Component.translatable("viscript_recipe.editor.remainder.mode." + serializedName);
    }
}
