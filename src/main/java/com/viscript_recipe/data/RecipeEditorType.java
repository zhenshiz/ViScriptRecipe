package com.viscript_recipe.data;

import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public record RecipeEditorType(
        ResourceLocation id, ResourceLocation category,
        String translationKey,
        Class<? extends IVSRecipeData> dataClass, Supplier<? extends IVSRecipeData> dataSupplier,
        BiFunction<NavigationView, RecipeEntry, RecipeCanvas<?>> canvasSupplier
) {
    public static RecipeEditorType of(ResourceLocation id, ResourceLocation category, String translationKey,
                                      Class<? extends IVSRecipeData> dataClass, Class<? extends RecipeCanvas<?>> canvas) {
        return new RecipeEditorType(id, category, translationKey, dataClass, dataSupplier(dataClass), canvasSupplier(canvas));
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    private static Supplier<? extends IVSRecipeData> dataSupplier(Class<? extends IVSRecipeData> dataClass) {
        return () -> {
            try {
                return dataClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) { return null; }
        };
    }

    private static BiFunction<NavigationView, RecipeEntry, RecipeCanvas<?>> canvasSupplier(Class<? extends RecipeCanvas<?>> canvasClass) {
        if (FMLEnvironment.dist.isDedicatedServer()) return null;
        return (view, entry) -> {
            try {
                return canvasClass.getDeclaredConstructor(NavigationView.class, RecipeEntry.class).newInstance(view, entry);
            } catch (Exception e) { return null; }
        };
    }
}
