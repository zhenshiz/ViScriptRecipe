package com.viscript_recipe.compat.justdirethings;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.justdirethings.canvas.JustDireSmithingCanvas;
import com.viscript_recipe.compat.justdirethings.canvas.JustDireTransformationCanvas;
import com.viscript_recipe.compat.justdirethings.data.JustDireSmithingData;
import com.viscript_recipe.compat.justdirethings.data.JustDireTransformationData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@LDLRegister(registry = IModModule.ID, name = JustDireRecipeEditorTypes.MOD_ID, modID = JustDireRecipeEditorTypes.MOD_ID)
public final class JustDireRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "justdirethings";
    public static final List<String> TYPES = List.of("goospread", "goospread_tag", "fluiddrop", "ability", "paxel");

    @Override
    public RecipeImportHandler importHandler() { return JustDireRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        for (var type : TYPES) {
            var icon = switch (type) {
                case "goospread", "goospread_tag" -> id("gooblock_tier1");
                case "fluiddrop" -> id("polymorphic_catalyst");
                default -> ResourceLocation.withDefaultNamespace("smithing_table");
            };
            registerCategory(RecipeEditorCategory.of(id(type), key(type), MOD_ID, id(type), icon));
            if (type.equals("ability") || type.equals("paxel")) {
                registerEditorType(RecipeEditorType.of(id(type), id(type), key(type),
                        JustDireSmithingData.class, JustDireSmithingCanvas.class));
            } else {
                registerEditorType(RecipeEditorType.of(id(type), id(type), key(type),
                        JustDireTransformationData.class, JustDireTransformationCanvas.class));
            }
        }
    }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    public static String key(String path) { return "viscript_recipe.editor.justdirethings." + path; }
}
