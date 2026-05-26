package com.viscript_recipe.recipe;

import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.Config;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeOperation;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;

public final class RecipeOverrideManager {
    private static final Object LOCK = new Object();
    @Nullable
    private static LinkedHashMap<ResourceLocation, RecipeHolder<?>> baseRecipes;
    private static ApplyResult lastResult = ApplyResult.empty();

    private RecipeOverrideManager() {
    }

    public static ApplyResult apply(RecipeManager recipeManager, HolderLookup.Provider provider) {
        synchronized (LOCK) {
            baseRecipes = snapshot(recipeManager.getRecipes());
            return applyOverrides(recipeManager, provider, baseRecipes);
        }
    }

    public static ApplyResult reload(RecipeManager recipeManager, HolderLookup.Provider provider) {
        synchronized (LOCK) {
            if (baseRecipes == null) {
                baseRecipes = snapshot(recipeManager.getRecipes());
            }
            return applyOverrides(recipeManager, provider, baseRecipes);
        }
    }

    public static ApplyResult getLastResult() {
        synchronized (LOCK) {
            return lastResult;
        }
    }

    private static ApplyResult applyOverrides(RecipeManager recipeManager, HolderLookup.Provider provider, LinkedHashMap<ResourceLocation, RecipeHolder<?>> base) {
        var loadedFiles = RecipeFileLoader.loadAll(provider);
        var showcaseOnly = Config.SHOWCASE_ONLY_VISCRIPT_RECIPES.get();
        var recipes = showcaseOnly ? new LinkedHashMap<ResourceLocation, RecipeHolder<?>>() : new LinkedHashMap<>(base);

        int entries = 0;
        int enabled = 0;
        int applied = 0;
        int skipped = 0;
        int failed = 0;
        for (var loaded : loadedFiles) {
            var file = loaded.file();
            if (file == null) {
                continue;
            }
            for (var entry : file.getEntries()) {
                entries++;
                if (!entry.isEnabled()) {
                    skipped++;
                    continue;
                }
                enabled++;
                switch (applyEntry(loaded.relativePath(), entry, recipes, showcaseOnly)) {
                    case APPLIED -> applied++;
                    case SKIPPED -> skipped++;
                    case FAILED -> failed++;
                }
            }
        }

        recipeManager.replaceRecipes(recipes.values());
        lastResult = new ApplyResult(
                loadedFiles.size(),
                entries,
                enabled,
                applied,
                skipped,
                failed,
                base.size(),
                recipes.size()
        );
        ViScriptRecipe.LOGGER.info(
                "Reloaded ViScriptRecipe overrides: {} files, {} entries, {} enabled, {} applied, {} skipped, {} failed",
                lastResult.fileCount(),
                lastResult.entryCount(),
                lastResult.enabledEntryCount(),
                lastResult.appliedEntryCount(),
                lastResult.skippedEntryCount(),
                lastResult.failedEntryCount()
        );
        if (showcaseOnly) {
            ViScriptRecipe.LOGGER.info(
                    "ViScriptRecipe showcase recipe mode is enabled: cleared {} base recipes before applying .recipe files",
                    base.size()
            );
        }
        return lastResult;
    }

    private static LinkedHashMap<ResourceLocation, RecipeHolder<?>> snapshot(Collection<RecipeHolder<?>> recipes) {
        var snapshot = new LinkedHashMap<ResourceLocation, RecipeHolder<?>>();
        for (var holder : recipes) {
            snapshot.put(holder.id(), holder);
        }
        return snapshot;
    }

    private static ApplyEntryResult applyEntry(String source, RecipeEntry entry, LinkedHashMap<ResourceLocation, RecipeHolder<?>> recipes, boolean showcaseOnly) {
        if (entry.getRecipeId() == null) {
            ViScriptRecipe.LOGGER.warn("Skipping recipe entry with empty id in {}", source);
            return ApplyEntryResult.FAILED;
        }
        var id = entry.getRecipeId();
        try {
            return switch (entry.getOperation()) {
                case REMOVE -> removeEntry(source, id, recipes, showcaseOnly);
                case ADD, REPLACE -> upsertEntry(source, entry, recipes, showcaseOnly);
            };
        } catch (Exception e) {
            ViScriptRecipe.LOGGER.error("Failed to apply recipe override {} from {}", id, source, e);
            return ApplyEntryResult.FAILED;
        }
    }

    private static ApplyEntryResult removeEntry(String source, ResourceLocation id, LinkedHashMap<ResourceLocation, RecipeHolder<?>> recipes, boolean showcaseOnly) {
        var removed = recipes.remove(id) != null;
        if (!removed) {
            if (!showcaseOnly) {
                ViScriptRecipe.LOGGER.warn("Recipe override {} tried to remove missing recipe {}", source, id);
            }
            return ApplyEntryResult.SKIPPED;
        }
        return ApplyEntryResult.APPLIED;
    }

    private static ApplyEntryResult upsertEntry(String source, RecipeEntry entry, LinkedHashMap<ResourceLocation, RecipeHolder<?>> recipes, boolean showcaseOnly) {
        var id = entry.getRecipeId();
        var exists = recipes.containsKey(id);
        if (entry.getOperation() == RecipeOperation.ADD && exists) {
            ViScriptRecipe.LOGGER.warn("Recipe override {} adds existing recipe {}; replacing it", source, id);
        } else if (entry.getOperation() == RecipeOperation.REPLACE && !exists && !showcaseOnly) {
            ViScriptRecipe.LOGGER.warn("Recipe override {} replaces missing recipe {}; adding it", source, id);
        }
        recipes.put(id, new RecipeHolder<>(id, entry.compile()));
        return ApplyEntryResult.APPLIED;
    }

    private enum ApplyEntryResult {
        APPLIED,
        SKIPPED,
        FAILED
    }

    public record ApplyResult(
            int fileCount,
            int entryCount,
            int enabledEntryCount,
            int appliedEntryCount,
            int skippedEntryCount,
            int failedEntryCount,
            int baseRecipeCount,
            int resultRecipeCount
    ) {
        public static ApplyResult empty() {
            return new ApplyResult(0, 0, 0, 0, 0, 0, 0, 0);
        }
    }
}
