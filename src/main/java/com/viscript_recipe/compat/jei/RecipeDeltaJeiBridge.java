package com.viscript_recipe.compat.jei;

import com.viscript_recipe.ViScriptRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.Map;
import java.util.Set;

/** 隔离可选的 JEI 类，避免同步和 RPC 层直接加载它们。 */
public final class RecipeDeltaJeiBridge {
    private static final String JEI = "jei";

    private RecipeDeltaJeiBridge() {
    }

    public static void applyDelta(
            long revision,
            Set<ResourceLocation> affectedRecipeIds,
            Map<ResourceLocation, RecipeHolder<?>> oldRecipes,
            Map<ResourceLocation, RecipeHolder<?>> newRecipes,
            Map<ResourceLocation, ResourceLocation> oldEditorTypes,
            Map<ResourceLocation, ResourceLocation> newEditorTypes,
            boolean arcaneAnvilChanged
    ) {
        if (!ViScriptRecipe.isModLoaded(JEI)) {
            return;
        }
        RecipeDeltaJeiSynchronizer.applyDelta(
                revision,
                affectedRecipeIds,
                oldRecipes,
                newRecipes,
                oldEditorTypes,
                newEditorTypes,
                arcaneAnvilChanged
        );
    }

    public static void applyBaseline() {
        if (ViScriptRecipe.isModLoaded(JEI)) {
            RecipeDeltaJeiSynchronizer.applyBaseline();
        }
    }
}
