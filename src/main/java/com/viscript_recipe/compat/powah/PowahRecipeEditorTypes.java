package com.viscript_recipe.compat.powah;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.powah.canvas.EnergizingCanvas;
import com.viscript_recipe.compat.powah.data.PowahRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = PowahRecipeEditorTypes.MOD_ID, modID = PowahRecipeEditorTypes.MOD_ID)
public final class PowahRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "powah";
    public static final ResourceLocation ENERGIZING = id("energizing");

    @Override
    public RecipeImportHandler importHandler() { return PowahRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        var title = "gui.powah.jei.category.energizing";
        var icon = id("energizing_orb");
        registerCategory(RecipeEditorCategory.of(ENERGIZING, title, MOD_ID, icon, icon));
        registerEditorType(RecipeEditorType.of(ENERGIZING, ENERGIZING, title, PowahRecipeData.class, EnergizingCanvas.class));
    }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    public static String key(String path) { return "viscript_recipe.editor.powah." + path; }
}
