package com.viscript_recipe;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    public static final String CONFIG_FILE_NAME = ViScriptRecipe.MOD_ID + "_config.toml";
    private static final String SHOWCASE_ONLY_PATH = "recipes.showcase_only_viscript_recipes";
    private static final String SYNC_TAGS_FOR_JEI_RELOAD_PATH = "recipes.sync_tags_for_jei_reload";

    public static final ModConfigSpec CONFIG_SPEC;
    public static final ModConfigSpec.BooleanValue SHOWCASE_ONLY_VISCRIPT_RECIPES;
    public static final ModConfigSpec.BooleanValue SYNC_TAGS_FOR_JEI_RELOAD;

    static {
        ModConfigSpec.Builder CONFIG_BUILDER = new ModConfigSpec.Builder();
        CONFIG_BUILDER.push("recipes");
        SHOWCASE_ONLY_VISCRIPT_RECIPES = CONFIG_BUILDER
                .translation("viscript_recipe.configuration.showcase_only_viscript_recipes")
                .define("showcase_only_viscript_recipes", false);
        SYNC_TAGS_FOR_JEI_RELOAD = CONFIG_BUILDER
                .translation("viscript_recipe.configuration.sync_tags_for_jei_reload")
                .define("sync_tags_for_jei_reload", true);
        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }

    public static void reloadRuntimeConfigFromDisk() {
        var path = configPath();
        if (path == null || !Files.isRegularFile(path)) {
            return;
        }
        try (var config = CommentedFileConfig.of(path)) {
            config.load();
            CONFIG_SPEC.correct(config);
            reloadBoolean(config, SHOWCASE_ONLY_PATH, SHOWCASE_ONLY_VISCRIPT_RECIPES, path);
            reloadBoolean(config, SYNC_TAGS_FOR_JEI_RELOAD_PATH, SYNC_TAGS_FOR_JEI_RELOAD, path);
        } catch (Exception e) {
            ViScriptRecipe.LOGGER.warn("Failed to reload ViScriptRecipe config from disk", e);
        }
    }

    private static void reloadBoolean(CommentedFileConfig config, String configPath, ModConfigSpec.BooleanValue value, Path filePath) {
        Object rawValue = config.get(configPath);
        if (rawValue instanceof Boolean booleanValue) {
            value.set(booleanValue);
            return;
        }
        ViScriptRecipe.LOGGER.warn("Ignoring invalid {} value in {}: {}", configPath, filePath, rawValue);
    }

    private static Path configPath() {
        for (var config : ModConfigs.getModConfigs(ViScriptRecipe.MOD_ID)) {
            if (config.getType() != ModConfig.Type.COMMON || !CONFIG_FILE_NAME.equals(config.getFileName())) {
                continue;
            }
            try {
                return config.getFullPath();
            } catch (IllegalStateException ignored) {
                return null;
            }
        }
        return null;
    }
}
