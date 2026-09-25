package com.viscript_recipe.compat.immersive_engineering;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.immersive_engineering.canvas.IECanvas;
import com.viscript_recipe.compat.immersive_engineering.data.IERecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

/** Registers the sixteen recipe-manager-backed JEI recipe types supplied by IE. */
@LDLRegister(registry = IModModule.ID, name = IERecipeEditorTypes.MOD_ID, modID = IERecipeEditorTypes.MOD_ID)
public final class IERecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "immersiveengineering";
    public static final String[] TYPES = {"coke_oven", "alloy", "blast_furnace", "blast_furnace_fuel",
            "cloche", "fertilizer", "metal_press", "crusher", "sawmill", "blueprint", "squeezer",
            "fermenter", "refinery", "arc_furnace", "mixer", "bottling_machine"};
    private static boolean registered;

    @Override public RecipeImportHandler importHandler() { return IERecipeImporter.INSTANCE; }

    @Override public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        for (String name : TYPES) {
            var id = id(name);
            var key = "viscript_recipe.editor.type.immersive_engineering." + name;
            registerCategory(RecipeEditorCategory.of(id, key, MOD_ID, id, icon(name)));
            registerEditorType(RecipeEditorType.of(id, id, key, IERecipeData.class, IERecipeData::new, IECanvas::new, MOD_ID));
        }
    }

    private static ResourceLocation icon(String name) {
        return id(switch (name) {
            case "alloy" -> "alloy_smelter";
            case "blast_furnace_fuel" -> "blast_furnace";
            case "fertilizer" -> "cloche";
            case "blueprint" -> "workbench";
            default -> name;
        });
    }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
}
