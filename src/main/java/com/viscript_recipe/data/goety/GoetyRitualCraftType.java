package com.viscript_recipe.data.goety;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum GoetyRitualCraftType implements StringRepresentable {
    ANIMATION("animation"),
    NECROTURGY("necroturgy"),
    FORGE("forge"),
    MAGIC("magic"),
    ADEPT_NETHER("adept_nether"),
    EXPERT_NETHER("expert_nether"),
    SABBATH("sabbath"),
    END("end"),
    SKY("sky"),
    STORM("storm"),
    GEOTURGY("geoturgy"),
    FROST("frost"),
    DEEP("deep"),
    OVERGROWN("overgrown"),
    DIVINATION("divination");

    private final String serializedName;

    GoetyRitualCraftType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }

    public static GoetyRitualCraftType byName(String name) {
        return Arrays.stream(values())
                .filter(value -> value.serializedName.equals(name))
                .findFirst()
                .orElse(MAGIC);
    }
}
