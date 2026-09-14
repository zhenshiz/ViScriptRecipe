package com.viscript_recipe.compat.confluence;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.confluence.canvas.ConfluenceCanvas;
import com.viscript_recipe.compat.confluence.data.ConfluenceRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

@LDLRegister(registry = IModModule.ID, name = ConfluenceRecipeEditorTypes.MOD_ID, modID = ConfluenceRecipeEditorTypes.MOD_ID)
public final class ConfluenceRecipeEditorTypes implements IModModule{
    public static final String MOD_ID = "confluence";
    public static final ResourceLocation ITEM_TRANSMUTATION = id("item_transmutation");
    public static final ResourceLocation SKY_MILL = id("sky_mill");
    public static final ResourceLocation ALTAR = id("altar");
    public static final ResourceLocation HELLFORGE = id("hellforge");
    public static final ResourceLocation HEAVY_WORK_BENCH = id("heavy_work_bench");
    public static final ResourceLocation ALCHEMY_TABLE = id("alchemy_table");
    public static final ResourceLocation FLETCHING_TABLE = id("fletching_table");
    public static final ResourceLocation COOKING_POT = id("cooking_pot");
    public static final ResourceLocation SAWMILL = id("sawmill");
    public static final ResourceLocation SOLIDIFIER = id("solidifier");
    public static final ResourceLocation HARDMODE_ANVIL = id("hardmode_anvil");
    public static final ResourceLocation HARDMODE_FORGE = id("hardmode_forge");
    public static final ResourceLocation LOOM = id("loom");
    public static final ResourceLocation DYE_VAT = id("dye_vat");
    public static final ResourceLocation CRYSTAL_BALL = id("crystal_ball");

    private static final Map<ResourceLocation, ResourceLocation> WORKSTATIONS = workstationMap();
    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return ConfluenceRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        for (var type : WORKSTATIONS.keySet()) {
            registerCategory(RecipeEditorCategory.of(
                    type, "viscript_recipe.editor.category.confluence." + type.getPath(),
                    MOD_ID, type, WORKSTATIONS.get(type)
            ));
            registerEditorType(RecipeEditorType.of(
                    type, type,
                    "viscript_recipe.editor.type.confluence." + type.getPath(),
                    ConfluenceRecipeData.class, ConfluenceRecipeData::new,
                    ConfluenceCanvas::new, MOD_ID
            ));
        }
    }

    public static boolean isType(ResourceLocation type) {
        return type != null && type.getNamespace().equals(MOD_ID) && WORKSTATIONS.containsKey(type);
    }

    public static boolean isEitherType(ResourceLocation type) {
        return HEAVY_WORK_BENCH.equals(type) || SAWMILL.equals(type)
                || HARDMODE_ANVIL.equals(type) || LOOM.equals(type);
    }

    public static boolean isEnvironmentType(ResourceLocation type) {
        return SKY_MILL.equals(type) || HEAVY_WORK_BENCH.equals(type) || CRYSTAL_BALL.equals(type);
    }

    public static boolean isAmountType(ResourceLocation type) {
        return SKY_MILL.equals(type) || ALTAR.equals(type) || HELLFORGE.equals(type)
                || HEAVY_WORK_BENCH.equals(type) || ALCHEMY_TABLE.equals(type)
                || COOKING_POT.equals(type) || SAWMILL.equals(type)
                || HARDMODE_ANVIL.equals(type) || HARDMODE_FORGE.equals(type)
                || LOOM.equals(type) || DYE_VAT.equals(type) || CRYSTAL_BALL.equals(type);
    }

    public static int maxInputs(ResourceLocation type) {
        if (ITEM_TRANSMUTATION.equals(type)) return 1;
        if (FLETCHING_TABLE.equals(type)) return 3;
        if (SKY_MILL.equals(type)) return 3;
        if (ALTAR.equals(type)) return 5;
        if (ALCHEMY_TABLE.equals(type)) return 7;
        if (HELLFORGE.equals(type) || HARDMODE_FORGE.equals(type)
                || COOKING_POT.equals(type) || DYE_VAT.equals(type)
                || CRYSTAL_BALL.equals(type)) return 4;
        return 16;
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private static Map<ResourceLocation, ResourceLocation> workstationMap() {
        var values = new LinkedHashMap<ResourceLocation, ResourceLocation>();
        values.put(ITEM_TRANSMUTATION, id("bottomless_shimmer_bucket"));
        values.put(SKY_MILL, id("sky_mill"));
        values.put(ALTAR, id("demon_altar"));
        values.put(HELLFORGE, id("hellforge"));
        values.put(HEAVY_WORK_BENCH, id("heavy_work_bench"));
        values.put(ALCHEMY_TABLE, id("alchemy_table"));
        values.put(FLETCHING_TABLE, ResourceLocation.withDefaultNamespace("fletching_table"));
        values.put(COOKING_POT, id("cooking_pot"));
        values.put(SAWMILL, id("sawmill"));
        values.put(SOLIDIFIER, id("solidifier"));
        values.put(HARDMODE_ANVIL, id("mythril_anvil"));
        values.put(HARDMODE_FORGE, id("adamantite_forge"));
        values.put(LOOM, id("loom"));
        values.put(DYE_VAT, id("dye_vat"));
        values.put(CRYSTAL_BALL, id("crystal_ball"));
        return Map.copyOf(values);
    }
}
