package com.viscript_recipe.data.confluence;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ConfluenceGamePhase implements StringRepresentable {
    BEFORE_SKELETRON,
    AFTER_SKELETRON,
    WALL_OF_FLESH,
    MECHANICAL_BOSSES,
    PLANTERA,
    GOLEM,
    MOON_LORD;

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
