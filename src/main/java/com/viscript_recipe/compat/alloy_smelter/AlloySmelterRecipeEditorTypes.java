package com.viscript_recipe.compat.alloy_smelter;

import com.viscript_recipe.compat.alloy_smelter.canvas.AlloySmelterCanvas;
import com.viscript_recipe.compat.alloy_smelter.data.AlloySmelterRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Registers the native Alloy Smelter recipe editor category. */
public final class AlloySmelterRecipeEditorTypes {
    public static final String MOD_ID = "alloy_smelter";
    public static final ResourceLocation SMELTING = id("smelting");
    private static boolean registered;

    private AlloySmelterRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                SMELTING,
                "viscript_recipe.editor.category.alloy_smelter.smelting",
                MOD_ID,
                List.of(MOD_ID),
                SMELTING,
                RecipeEditorLayout.ALLOY_SMELTER,
                id("forge_controller_tier1")
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                SMELTING, SMELTING,
                "viscript_recipe.editor.type.alloy_smelter.smelting",
                AlloySmelterRecipeData.class, AlloySmelterRecipeData::new,
                AlloySmelterCanvas::new, MOD_ID
        ));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
