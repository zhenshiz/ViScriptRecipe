package com.viscript_recipe.compat.extradelight;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.extradelight.canvas.ExtraDelightRecipeCanvas;
import com.viscript_recipe.compat.extradelight.data.*;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

/** 注册 JEI 的额外工作站配方及具有原生加工行为的动态食品配方。 */
@LDLRegister(registry = IModModule.ID, name = ExtraDelightRecipeEditorTypes.MOD_ID, modID = ExtraDelightRecipeEditorTypes.MOD_ID)
public final class ExtraDelightRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "extradelight";

    @Override
    public RecipeImportHandler importHandler() { return ExtraDelightRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        register("mortar", "extradelight:mortar_stone", ExtraDelightMortarRecipeData.class);
        register("mixing_bowl", "extradelight:mixing_bowl", ExtraDelightMixingBowlRecipeData.class);
        register("oven", "extradelight:oven", ExtraDelightOvenRecipeData.class);
        register("drying_rack", "extradelight:drying_rack", ExtraDelightDryingRackRecipeData.class);
        register("dough_shaping", "extradelight:dough_shaping", ExtraDelightDoughShapingRecipeData.class);
        register("tool_on_block", "extradelight:gingerbread_cookie_block_item", ExtraDelightToolOnBlockRecipeData.class);
        register("feast", "extradelight:curry_feast", ExtraDelightFeastRecipeData.class);
        register("melting_pot", "extradelight:melting_pot", ExtraDelightMeltingPotRecipeData.class);
        register("chiller", "extradelight:chiller", ExtraDelightChillerRecipeData.class);
        register("vat", "extradelight:vat", ExtraDelightVatRecipeData.class);
        register("evaporator", "extradelight:evaporator", ExtraDelightEvaporatorRecipeData.class);
        register("bottle_fluid", "minecraft:glass_bottle", ExtraDelightBottleFluidRecipeData.class);
        register("shaped_jar", "minecraft:crafting_table", ExtraDelightShapedJarRecipeData.class);
        register("juicer", "extradelight:juicer", ExtraDelightJuicerRecipeData.class);
        register("dynamic_jam", "farmersdelight:cooking_pot", ExtraDelightDynamicJamRecipeData.class);
        register("dynamic_toast", "crafting_table", ExtraDelightDynamicToastRecipeData.class);
    }

    private void register(String id, String workstationItemId, Class<? extends IVSRecipeData> dataClass) {
        registerCategory(RecipeEditorCategory.of(id(id), key(id), MOD_ID, id(id), ResourceLocation.parse(workstationItemId)));
        registerEditorType(RecipeEditorType.of(id(id), id(id), key(id), dataClass, ExtraDelightRecipeCanvas.class));
    }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    public static String key(String path) { return "viscript_recipe.editor.extradelight." + path; }
}
