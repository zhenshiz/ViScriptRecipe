package com.viscript_recipe.data.mekanism;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.world.item.ItemStack;

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
            RecipeEditorTypes.register(new RecipeEditorType(
                    kind.typeId(),
                    kind.typeId(),
                    "viscript_recipe.editor.type.mekanism." + path,
                    REQUIRED_MODS,
                    false,
                    entry -> entry.getMekanism().compile(kind.typeId()),
                    entry -> false,
                    (entry, value) -> {
                    },
                    entry -> kind.itemOutputs() > 0 ? entry.getMekanism().getItemOutput() : ItemStack.EMPTY,
                    (entry, stack) -> entry.getMekanism().setItemOutput(stack == null ? ItemStack.EMPTY : stack.copy())
            ));
        }
    }
}
