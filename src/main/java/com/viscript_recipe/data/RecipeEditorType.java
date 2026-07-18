package com.viscript_recipe.data;

import com.viscript_recipe.ViScriptRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public record RecipeEditorType(
        ResourceLocation id, ResourceLocation category,
        String translationKey,
        Class<? extends IVSRecipeData> dataClass, Supplier<? extends IVSRecipeData> dataSupplier,
        String... requiredMods
) {
    public static RecipeEditorType of(ResourceLocation id, ResourceLocation category,
                                      String translationKey, Class<? extends IVSRecipeData> dataClass,
                                      Supplier<? extends IVSRecipeData> dataSupplier, String... requiredMods) {
        return new RecipeEditorType(id, category, translationKey, dataClass, dataSupplier, requiredMods);
    }

    public boolean isAvailable() {
        for (var modId : requiredMods) {
            if (!ViScriptRecipe.isModLoaded(modId)) return false;
        }
        return true;
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }
}
