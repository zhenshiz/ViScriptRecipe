package com.viscript_recipe.data;

import com.viscript_recipe.ViScriptRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record RecipeEditorCategory(
        ResourceLocation id,
        String translationKey,
        String ownerTranslationKey,
        List<String> requiredMods,
        ResourceLocation defaultType,
        RecipeEditorLayout layout
) {
    public RecipeEditorCategory {
        requiredMods = requiredMods == null ? List.of() : List.copyOf(requiredMods);
    }

    public boolean isAvailable() {
        for (var modId : requiredMods) {
            if (!ViScriptRecipe.isModLoaded(modId)) {
                return false;
            }
        }
        return true;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public Component ownerName() {
        return Component.translatable(ownerTranslationKey);
    }
}
