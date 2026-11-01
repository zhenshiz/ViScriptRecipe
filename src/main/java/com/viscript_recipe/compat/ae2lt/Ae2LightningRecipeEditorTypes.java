package com.viscript_recipe.compat.ae2lt;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.ae2lt.canvas.Ae2LightningRecipeCanvas;
import com.viscript_recipe.compat.ae2lt.data.Ae2LightningRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** 注册闪电科技的七种数据驱动加工配方。 */
@LDLRegister(registry = IModModule.ID, name = Ae2LightningRecipeEditorTypes.MOD_ID, modID = Ae2LightningRecipeEditorTypes.MOD_ID)
public final class Ae2LightningRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "ae2lt";
    public static final List<String> TYPES = List.of("lightning_transform", "lightning_simulation", "lightning_assembly",
            "overload_processing", "crystal_catalyzer", "firmament_conversion", "lightning_strike");
    private static final List<String> ICONS = List.of("tesla_coil", "lightning_simulation_room", "lightning_assembly_chamber",
            "overload_processing_factory", "crystal_catalyzer", "firmament_conversion_core", "tesla_coil");

    @Override
    public RecipeImportHandler importHandler() { return Ae2LightningRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        for (int i = 0; i < TYPES.size(); i++) {
            var type = id(TYPES.get(i));
            var title = "jei.ae2lt." + type.getPath() + ".title";
            registerCategory(RecipeEditorCategory.of(type, title, MOD_ID, type, id(ICONS.get(i))));
            registerEditorType(RecipeEditorType.of(type, type, title,
                    Ae2LightningRecipeData.class, Ae2LightningRecipeCanvas.class));
        }
    }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    public static String key(String path) { return "viscript_recipe.editor.ae2lt." + path; }
}
