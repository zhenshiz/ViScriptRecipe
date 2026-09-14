package com.viscript_recipe.compat.alloy_smelter;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.alloy_smelter.canvas.AlloySmelterCanvas;
import com.viscript_recipe.compat.alloy_smelter.data.AlloySmelterRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = AlloySmelterRecipeEditorTypes.MOD_ID, modID = AlloySmelterRecipeEditorTypes.MOD_ID)
public final class AlloySmelterRecipeEditorTypes implements IModModule{
    public static final String MOD_ID = "alloy_smelter";
    public static final ResourceLocation SMELTING = id("smelting");
    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return AlloySmelterRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategory(RecipeEditorCategory.of(
                SMELTING, "viscript_recipe.editor.category.alloy_smelter.smelting",
                MOD_ID, SMELTING, id("forge_controller_tier1")
        ));
        registerEditorType(RecipeEditorType.of(
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
