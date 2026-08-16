package com.viscript_recipe.compat.touhou_little_maid;

import com.viscript_recipe.compat.touhou_little_maid.canvas.AltarCanvas;
import com.viscript_recipe.compat.touhou_little_maid.data.TouhouLittleMaidAltarRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

public final class TouhouLittleMaidRecipeEditorTypes {
    public static final String MOD_ID = "touhou_little_maid";

    public static final ResourceLocation ALTAR = touhouLittleMaid("altar");
    public static final ResourceLocation ALTAR_RECIPE = touhouLittleMaid("altar_recipe");

    private static boolean registered;

    private TouhouLittleMaidRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                ALTAR,
                "viscript_recipe.editor.category.touhou_little_maid.altar",
                MOD_ID, ALTAR_RECIPE, touhouLittleMaid("hakurei_gohei")
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                ALTAR_RECIPE, ALTAR,
                "viscript_recipe.editor.type.touhou_little_maid.altar",
                TouhouLittleMaidAltarRecipeData.class, TouhouLittleMaidAltarRecipeData::new,
                AltarCanvas::new, MOD_ID
        ));
    }

    public static ResourceLocation touhouLittleMaid(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
