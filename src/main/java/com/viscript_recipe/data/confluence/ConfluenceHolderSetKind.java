package com.viscript_recipe.data.confluence;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ConfluenceHolderSetKind implements StringRepresentable {
    NONE,
    IDS,
    TAG;

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
