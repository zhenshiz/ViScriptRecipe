package com.viscript_recipe.recipe.importer;

import com.viscript_recipe.data.RecipeEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Describes the entries produced by one recipe import request. */
public record RecipeImportResult(boolean successful, List<RecipeEntry> entries, Component message) {
    public static RecipeImportResult success(RecipeEntry entry, Component message) {
        return success(List.of(entry), message);
    }

    public static RecipeImportResult success(List<RecipeEntry> entries, Component message) {
        if (entries == null || entries.isEmpty()) {
            throw new IllegalArgumentException("A successful recipe import must contain at least one entry");
        }
        return new RecipeImportResult(true, List.copyOf(entries), message);
    }

    public static RecipeImportResult failure(Component message) {
        return new RecipeImportResult(false, List.of(), message);
    }

    public static RecipeImportResult failure(String key, Object... args) {
        return failure(Component.translatable(key, args));
    }

    /**
     * Returns the first imported entry for callers that only consume one entry.
     *
     * @return the first imported entry, or {@code null} when this result contains no entry
     */
    public @Nullable RecipeEntry entry() {
        return entries.isEmpty() ? null : entries.getFirst();
    }
}
