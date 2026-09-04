package com.viscript_recipe.compat.industrial_foregoing;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.industrial_foregoing.canvas.*;
import com.viscript_recipe.compat.industrial_foregoing.data.*;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Supplier;

@LDLRegister(registry = IModModule.ID, name = IndustrialForegoingRecipeEditorTypes.MOD_ID, modID = IndustrialForegoingRecipeEditorTypes.MOD_ID)
public final class IndustrialForegoingRecipeEditorTypes implements IModModule{
    public static final String MOD_ID = "industrialforegoing";
    public static final ResourceLocation CRUSHER = id("crusher");
    public static final ResourceLocation DISSOLUTION_CHAMBER = id("dissolution_chamber");
    public static final ResourceLocation FLUID_EXTRACTOR = id("fluid_extractor");
    public static final ResourceLocation LASER_DRILL_ORE = id("laser_drill_ore");
    public static final ResourceLocation LASER_DRILL_FLUID = id("laser_drill_fluid");
    public static final ResourceLocation STONEWORK_GENERATE = id("stonework_generate");

    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return IndustrialForegoingRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategory(CRUSHER, CRUSHER, "material_stonework_factory");
        registerCategory(DISSOLUTION_CHAMBER, DISSOLUTION_CHAMBER, "dissolution_chamber");
        registerCategory(FLUID_EXTRACTOR, FLUID_EXTRACTOR, "fluid_extractor");
        registerCategory(LASER_DRILL_ORE, LASER_DRILL_ORE, "laser_drill");
        registerCategory(LASER_DRILL_FLUID, LASER_DRILL_FLUID, "laser_drill");
        registerCategory(STONEWORK_GENERATE, STONEWORK_GENERATE, "material_stonework_factory");

        register(CRUSHER, IndustrialCrusherRecipeData.class, IndustrialCrusherRecipeData::new, CrusherCanvas::new);
        register(DISSOLUTION_CHAMBER, IndustrialDissolutionRecipeData.class, IndustrialDissolutionRecipeData::new, DissolutionCanvas::new);
        register(FLUID_EXTRACTOR, IndustrialFluidExtractorRecipeData.class, IndustrialFluidExtractorRecipeData::new, FluidExtractorCanvas::new);
        register(LASER_DRILL_ORE, IndustrialLaserDrillOreRecipeData.class, IndustrialLaserDrillOreRecipeData::new, LaserDrillOreCanvas::new);
        register(LASER_DRILL_FLUID, IndustrialLaserDrillFluidRecipeData.class, IndustrialLaserDrillFluidRecipeData::new, LaserDrillFluidCanvas::new);
        register(STONEWORK_GENERATE, IndustrialStoneWorkRecipeData.class, IndustrialStoneWorkRecipeData::new, StoneWorkCanvas::new);
    }

    private void registerCategory(ResourceLocation id, ResourceLocation defaultType, String workstationPath) {
        registerCategory(RecipeEditorCategory.of(
                id, "viscript_recipe.editor.category.industrial_foregoing." + id.getPath(),
                MOD_ID, defaultType, IndustrialForegoingRecipeEditorTypes.id(workstationPath)
        ));
    }

    private void register(ResourceLocation id,
                                 Class<? extends IVSRecipeData> dataClass, Supplier<? extends IVSRecipeData> dataSupplier,
                                 BiFunction<NavigationView, RecipeEntry, RecipeCanvas<?>> canvasSupplier) {
        registerEditorType(RecipeEditorType.of(id, id,
                "viscript_recipe.editor.type.industrial_foregoing." + id.getPath(),
                dataClass, dataSupplier, canvasSupplier, MOD_ID
        ));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
