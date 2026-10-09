package com.viscript_recipe.compat.enderio;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.enderio.canvas.EnderIoRecipeCanvas;
import com.viscript_recipe.compat.enderio.data.EnderIoRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

@LDLRegister(registry = IModModule.ID, name = EnderIoRecipeEditorTypes.MOD_ID, modID = EnderIoRecipeEditorTypes.MOD_ID)
public final class EnderIoRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "enderio";
    public static final List<String> TYPES = List.of("fire_crafting", "alloy_smelting", "enchanting", "sag_milling",
            "slicing", "soul_binding", "tank", "vat_fermenting", "weather_change", "shaped_entity_storage");

    @Override
    public RecipeImportHandler importHandler() { return EnderIoRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        for (var type : TYPES) {
            var icon = switch (type) {
                case "fire_crafting" -> id("grains_of_infinity");
                case "alloy_smelting" -> id("alloy_smelter");
                case "enchanting" -> id("enchanter");
                case "sag_milling" -> id("sag_mill");
                case "slicing" -> id("slice_and_splice");
                case "soul_binding" -> id("soul_binder");
                case "tank" -> id("fluid_tank");
                case "vat_fermenting" -> id("vat");
                case "weather_change" -> id("weather_obelisk");
                default -> ResourceLocation.parse("minecraft:crafting_table");
            };
            registerCategory(RecipeEditorCategory.of(id(type), key(type), MOD_ID, id(type), icon));
            registerEditorType(RecipeEditorType.of(id(type), id(type), key(type), EnderIoRecipeData.class, EnderIoRecipeCanvas.class));
        }
    }

    public static String key(String type) { return "viscript_recipe.editor.type.enderio." + type; }
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
}
