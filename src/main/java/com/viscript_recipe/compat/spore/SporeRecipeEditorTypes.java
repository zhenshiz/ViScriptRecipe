package com.viscript_recipe.compat.spore;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.spore.canvas.GraftingCanvas;
import com.viscript_recipe.compat.spore.canvas.SurgeryCanvas;
import com.viscript_recipe.compat.spore.data.SporeGraftingRecipeData;
import com.viscript_recipe.compat.spore.data.SporeSurgeryRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = SporeRecipeEditorTypes.MOD_ID, modID = SporeRecipeEditorTypes.MOD_ID)
public final class SporeRecipeEditorTypes implements IModModule{
    public static final String MOD_ID = "spore";

    public static final ResourceLocation SURGERY_TABLE = spore("surgery_table");
    public static final ResourceLocation SURGERY = spore("surgery");
    public static final ResourceLocation GRAFTING = spore("grafting");

    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return SporeRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategory(RecipeEditorCategory.of(
                SURGERY_TABLE,
                "viscript_recipe.editor.category.spore.surgery_table",
                MOD_ID, SURGERY, SURGERY_TABLE
        ));
        registerTypes();
    }

    private void registerTypes() {
        registerEditorType(RecipeEditorType.of(
                SURGERY, SURGERY_TABLE,
                "viscript_recipe.editor.type.spore.surgery",
                SporeSurgeryRecipeData.class, SporeSurgeryRecipeData::new,
                SurgeryCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                GRAFTING, SURGERY_TABLE,
                "viscript_recipe.editor.type.spore.grafting",
                SporeGraftingRecipeData.class, SporeGraftingRecipeData::new,
                GraftingCanvas::new, MOD_ID
        ));
    }

    public static ResourceLocation spore(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
