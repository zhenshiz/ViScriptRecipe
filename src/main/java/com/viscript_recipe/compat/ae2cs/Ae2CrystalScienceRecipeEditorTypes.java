package com.viscript_recipe.compat.ae2cs;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.ae2cs.canvas.Ae2CrystalScienceRecipeCanvas;
import com.viscript_recipe.compat.ae2cs.data.Ae2CrystalScienceRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** 注册晶体科学的三种数据驱动机器配方。 */
@LDLRegister(registry = IModModule.ID, name = Ae2CrystalScienceRecipeEditorTypes.MOD_ID, modID = Ae2CrystalScienceRecipeEditorTypes.MOD_ID)
public final class Ae2CrystalScienceRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "ae2cs";
    public static final List<String> TYPES = List.of("circuit_etcher", "crystal_aggregator", "crystal_pulverizer");

    @Override
    public RecipeImportHandler importHandler() { return Ae2CrystalScienceRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        for (var name : TYPES) {
            var type = id(name);
            var title = "ae2cs.integration.jei.recipe_category." + name;
            registerCategory(RecipeEditorCategory.of(type, title, MOD_ID, type, type));
            registerEditorType(RecipeEditorType.of(type, type, title,
                    Ae2CrystalScienceRecipeData.class, Ae2CrystalScienceRecipeCanvas.class));
        }
    }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    public static String key(String path) { return "viscript_recipe.editor.ae2cs." + path; }
}
