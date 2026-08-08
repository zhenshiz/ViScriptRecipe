package com.viscript_recipe.data.confluence;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ConfluenceCraftingMode implements StringRepresentable {
    SHAPED,
    SHAPELESS;

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
