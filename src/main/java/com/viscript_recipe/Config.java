package com.viscript_recipe;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec CONFIG_SPEC;
    public static final ModConfigSpec.BooleanValue SHOWCASE_ONLY_VISCRIPT_RECIPES;

    static {
        ModConfigSpec.Builder CONFIG_BUILDER = new ModConfigSpec.Builder();
        CONFIG_BUILDER.push("recipes");
        SHOWCASE_ONLY_VISCRIPT_RECIPES = CONFIG_BUILDER
                .translation("viscript_recipe.configuration.showcase_only_viscript_recipes")
                .define("showcase_only_viscript_recipes", false);
        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }
}
