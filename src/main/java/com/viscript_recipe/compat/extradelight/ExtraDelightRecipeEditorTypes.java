package com.viscript_recipe.compat.extradelight;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.extradelight.data.*;
import com.viscript_recipe.compat.extradelight.canvas.*;
import com.viscript_recipe.data.*;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

/** 注册 JEI 的额外工作站配方及具有原生加工行为的动态食品配方。 */
@LDLRegister(registry = IModModule.ID, name = ExtraDelightRecipeEditorTypes.MOD_ID, modID = ExtraDelightRecipeEditorTypes.MOD_ID)
public final class ExtraDelightRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "extradelight";
    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() { return ExtraDelightRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategory(RecipeEditorCategory.of(id("mortar"), key("mortar"), MOD_ID, id("mortar"), ResourceLocation.parse("extradelight:mortar_stone")));
        registerEditorType(RecipeEditorType.of(id("mortar"), id("mortar"), key("mortar"),
                ExtraDelightMortarRecipeData.class, ExtraDelightMortarRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("mixing_bowl"), key("mixing_bowl"), MOD_ID, id("mixing_bowl"), ResourceLocation.parse("extradelight:mixing_bowl")));
        registerEditorType(RecipeEditorType.of(id("mixing_bowl"), id("mixing_bowl"), key("mixing_bowl"),
                ExtraDelightMixingBowlRecipeData.class, ExtraDelightMixingBowlRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("oven"), key("oven"), MOD_ID, id("oven"), ResourceLocation.parse("extradelight:oven")));
        registerEditorType(RecipeEditorType.of(id("oven"), id("oven"), key("oven"),
                ExtraDelightOvenRecipeData.class, ExtraDelightOvenRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("drying_rack"), key("drying_rack"), MOD_ID, id("drying_rack"), ResourceLocation.parse("extradelight:drying_rack")));
        registerEditorType(RecipeEditorType.of(id("drying_rack"), id("drying_rack"), key("drying_rack"),
                ExtraDelightDryingRackRecipeData.class, ExtraDelightDryingRackRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("dough_shaping"), key("dough_shaping"), MOD_ID, id("dough_shaping"), ResourceLocation.parse("extradelight:dough_shaping")));
        registerEditorType(RecipeEditorType.of(id("dough_shaping"), id("dough_shaping"), key("dough_shaping"),
                ExtraDelightDoughShapingRecipeData.class, ExtraDelightDoughShapingRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("tool_on_block"), key("tool_on_block"), MOD_ID, id("tool_on_block"), ResourceLocation.parse("extradelight:gingerbread_cookie_block_item")));
        registerEditorType(RecipeEditorType.of(id("tool_on_block"), id("tool_on_block"), key("tool_on_block"),
                ExtraDelightToolOnBlockRecipeData.class, ExtraDelightToolOnBlockRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("feast"), key("feast"), MOD_ID, id("feast"), ResourceLocation.parse("extradelight:curry_feast")));
        registerEditorType(RecipeEditorType.of(id("feast"), id("feast"), key("feast"),
                ExtraDelightFeastRecipeData.class, ExtraDelightFeastRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("melting_pot"), key("melting_pot"), MOD_ID, id("melting_pot"), ResourceLocation.parse("extradelight:melting_pot")));
        registerEditorType(RecipeEditorType.of(id("melting_pot"), id("melting_pot"), key("melting_pot"),
                ExtraDelightMeltingPotRecipeData.class, ExtraDelightMeltingPotRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("chiller"), key("chiller"), MOD_ID, id("chiller"), ResourceLocation.parse("extradelight:chiller")));
        registerEditorType(RecipeEditorType.of(id("chiller"), id("chiller"), key("chiller"),
                ExtraDelightChillerRecipeData.class, ExtraDelightChillerRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("vat"), key("vat"), MOD_ID, id("vat"), ResourceLocation.parse("extradelight:vat")));
        registerEditorType(RecipeEditorType.of(id("vat"), id("vat"), key("vat"),
                ExtraDelightVatRecipeData.class, ExtraDelightVatRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("evaporator"), key("evaporator"), MOD_ID, id("evaporator"), ResourceLocation.parse("extradelight:evaporator")));
        registerEditorType(RecipeEditorType.of(id("evaporator"), id("evaporator"), key("evaporator"),
                ExtraDelightEvaporatorRecipeData.class, ExtraDelightEvaporatorRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("bottle_fluid"), key("bottle_fluid"), MOD_ID, id("bottle_fluid"), ResourceLocation.parse("minecraft:glass_bottle")));
        registerEditorType(RecipeEditorType.of(id("bottle_fluid"), id("bottle_fluid"), key("bottle_fluid"),
                ExtraDelightBottleFluidRecipeData.class, ExtraDelightBottleFluidRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("shaped_jar"), key("shaped_jar"), MOD_ID, id("shaped_jar"), ResourceLocation.parse("minecraft:crafting_table")));
        registerEditorType(RecipeEditorType.of(id("shaped_jar"), id("shaped_jar"), key("shaped_jar"),
                ExtraDelightShapedJarRecipeData.class, ExtraDelightShapedJarRecipeData::new, ExtraDelightShapedJarCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("juicer"), key("juicer"), MOD_ID, id("juicer"), ResourceLocation.parse("extradelight:juicer")));
        registerEditorType(RecipeEditorType.of(id("juicer"), id("juicer"), key("juicer"),
                ExtraDelightJuicerRecipeData.class, ExtraDelightJuicerRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("dynamic_jam"), key("dynamic_jam"), MOD_ID, id("dynamic_jam"), ResourceLocation.parse("farmersdelight:cooking_pot")));
        registerEditorType(RecipeEditorType.of(id("dynamic_jam"), id("dynamic_jam"), key("dynamic_jam"),
                ExtraDelightDynamicJamRecipeData.class, ExtraDelightDynamicJamRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
        registerCategory(RecipeEditorCategory.of(id("dynamic_toast"), key("dynamic_toast"), MOD_ID, id("dynamic_toast"), ResourceLocation.parse("minecraft:crafting_table")));
        registerEditorType(RecipeEditorType.of(id("dynamic_toast"), id("dynamic_toast"), key("dynamic_toast"),
                ExtraDelightDynamicToastRecipeData.class, ExtraDelightDynamicToastRecipeData::new, ExtraDelightRecipeCanvas::new, MOD_ID));
    }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    public static String key(String path) { return "viscript_recipe.editor.extradelight." + path; }
}
