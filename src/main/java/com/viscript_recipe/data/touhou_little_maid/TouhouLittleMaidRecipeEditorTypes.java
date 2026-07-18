package com.viscript_recipe.data.touhou_little_maid;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class TouhouLittleMaidRecipeEditorTypes {
    public static final String MOD_ID = "touhou_little_maid";

    public static final ResourceLocation ALTAR = touhouLittleMaid("altar");
    public static final ResourceLocation ALTAR_RECIPE = touhouLittleMaid("altar_recipe");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private TouhouLittleMaidRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                ALTAR,
                "viscript_recipe.editor.category.touhou_little_maid.altar",
                MOD_ID,
                REQUIRED_MODS,
                ALTAR_RECIPE,
                RecipeEditorLayout.TOUHOU_LITTLE_MAID_ALTAR,
                touhouLittleMaid("hakurei_gohei")
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                ALTAR_RECIPE, ALTAR,
                "viscript_recipe.editor.type.touhou_little_maid.altar",
                TouhouLittleMaidAltarRecipeData.class, TouhouLittleMaidAltarRecipeData::new,
                MOD_ID
        ));
    }

    public static ResourceLocation touhouLittleMaid(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
