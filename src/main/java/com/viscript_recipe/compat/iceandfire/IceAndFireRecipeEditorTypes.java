package com.viscript_recipe.compat.iceandfire;

import com.viscript_recipe.compat.iceandfire.canvas.DragonForgeCanvas;
import com.viscript_recipe.compat.iceandfire.data.DragonForgeRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

public final class IceAndFireRecipeEditorTypes {
    public static final String MOD_ID = "iceandfire";

    public static final ResourceLocation DRAGON_FORGE = iceandfire("dragon_forge");
    public static final ResourceLocation DRAGONFORGE = iceandfire("dragonforge");

    private static boolean registered;

    private IceAndFireRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        registerCategories();
        registerTypes();
    }

    private static void registerCategories() {
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                DRAGON_FORGE,
                "viscript_recipe.editor.category.iceandfire.dragon_forge",
                MOD_ID, DRAGONFORGE, iceandfire("dragonforge_fire_core")
        ));
    }

    private static void registerTypes() {
        RecipeEditorTypes.register(RecipeEditorType.of(
                DRAGONFORGE, DRAGON_FORGE,
                "viscript_recipe.editor.type.iceandfire.dragonforge",
                DragonForgeRecipeData.class, DragonForgeRecipeData::new,
                DragonForgeCanvas::new, MOD_ID
        ));
    }

    private static ResourceLocation iceandfire(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
