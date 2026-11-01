package com.viscript_recipe.compat.neoecoae;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.neoecoae.canvas.NeoEcoRecipeCanvas;
import com.viscript_recipe.compat.neoecoae.data.NeoEcoRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** 注册 Neo ECO 的冷却、集成工作站与大型工作站配方。 */
@LDLRegister(registry = IModModule.ID, name = NeoEcoRecipeEditorTypes.MOD_ID, modID = NeoEcoRecipeEditorTypes.MOD_ID)
public final class NeoEcoRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "neoecoae";
    public static final List<String> TYPES = List.of("cooling", "integrated_working_station", "large_integrated_working_station");

    @Override
    public RecipeImportHandler importHandler() { return NeoEcoRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        for (var name : TYPES) {
            var type = id(name);
            var title = "category.neoecoae." + name;
            var icon = id(name.equals("cooling") ? "crafting_system_l9" : "integrated_working_station");
            registerCategory(RecipeEditorCategory.of(type, title, MOD_ID, icon, icon));
            registerEditorType(RecipeEditorType.of(type, type, title,
                    NeoEcoRecipeData.class, NeoEcoRecipeCanvas.class));
        }
    }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    public static String key(String path) { return "viscript_recipe.editor.neoecoae." + path; }
}
