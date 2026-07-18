package com.viscript_recipe.data.mekanism;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;

import java.util.List;

/**
 * Registers the editable recipe serializers supplied by the base Mekanism module.
 */
public final class MekanismRecipeEditorTypes {
    public static final String MOD_ID = "mekanism";
    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private MekanismRecipeEditorTypes() {
    }

    /**
     * Registers every supported Mekanism serializer once.
     */
    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        for (var kind : MekanismRecipeKind.values()) {
            var path = kind.typeId().getPath();
            RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                    kind.typeId(),
                    "viscript_recipe.editor.category.mekanism." + path,
                    MOD_ID,
                    REQUIRED_MODS,
                    kind.typeId(),
                    RecipeEditorLayout.MEKANISM,
                    kind.workstationId()
            ));
            RecipeEditorTypes.register(RecipeEditorType.of(
                    kind.typeId(), kind.typeId(),
                    "viscript_recipe.editor.type.mekanism." + path,
                    MekanismRecipeData.class, MekanismRecipeData::new,
                    MOD_ID
            ));
        }
    }
}
