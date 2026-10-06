package com.viscript_recipe.compat.tacz;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.tacz.canvas.TaczWorkbenchCanvas;
import com.viscript_recipe.compat.tacz.data.TaczRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

/** 注册 TACZ 枪械工作台的配方编辑类型。 */
@LDLRegister(registry = IModModule.ID, name = TaczRecipeEditorTypes.MOD_ID, modID = TaczRecipeEditorTypes.MOD_ID)
public final class TaczRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "tacz";
    public static final ResourceLocation CRAFTING = ResourceLocation.fromNamespaceAndPath(MOD_ID, "gun_smith_table_crafting");
    public static final String NAME_KEY = "viscript_recipe.editor.tacz.workbench";
    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() { return TaczRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategory(RecipeEditorCategory.of(CRAFTING, NAME_KEY, MOD_ID, CRAFTING,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "workbench_a")));
        registerEditorType(RecipeEditorType.of(CRAFTING, CRAFTING, NAME_KEY,
                TaczRecipeData.class, TaczRecipeData::new, TaczWorkbenchCanvas::new, MOD_ID));
    }
}
