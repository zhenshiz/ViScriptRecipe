package com.viscript_recipe.compat.omnisequence;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.omnisequence.canvas.OmniSequenceRecipeCanvas;
import com.viscript_recipe.compat.omnisequence.data.OmniSequenceRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** 注册构筑井加工、研究和整机导入声明的编辑入口。 */
@LDLRegister(registry = IModModule.ID, name = OmniSequenceRecipeEditorTypes.MOD_ID, modID = OmniSequenceRecipeEditorTypes.MOD_ID)
public final class OmniSequenceRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "molecularmanipulator";
    public static final List<String> TYPES = List.of("matter_fabrication", "matter_research", "matter_machine_import");

    @Override
    public RecipeImportHandler importHandler() { return OmniSequenceRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        var category = id("matter_fabrication");
        registerCategory(RecipeEditorCategory.of(category, key("workstation"), MOD_ID, category, id("matter_fabrication_controller")));
        for (var type : TYPES) registerEditorType(RecipeEditorType.of(id(type), category, key(type),
                OmniSequenceRecipeData.class, OmniSequenceRecipeCanvas.class));
    }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    public static String key(String path) { return "viscript_recipe.editor.omnisequence." + path; }
}
