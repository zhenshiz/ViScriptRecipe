package com.viscript_recipe.compat.industrial_foregoing;

import com.viscript_recipe.compat.industrial_foregoing.canvas.*;
import com.viscript_recipe.compat.industrial_foregoing.data.*;
import com.viscript_recipe.data.*;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class IndustrialForegoingRecipeEditorTypes {
    public static final String MOD_ID = "industrialforegoing";
    public static final ResourceLocation CRUSHER = id("crusher");
    public static final ResourceLocation DISSOLUTION_CHAMBER = id("dissolution_chamber");
    public static final ResourceLocation FLUID_EXTRACTOR = id("fluid_extractor");
    public static final ResourceLocation LASER_DRILL_ORE = id("laser_drill_ore");
    public static final ResourceLocation LASER_DRILL_FLUID = id("laser_drill_fluid");
    public static final ResourceLocation STONEWORK_GENERATE = id("stonework_generate");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private IndustrialForegoingRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
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

    private static void registerCategory(ResourceLocation id, ResourceLocation defaultType, String workstationPath) {
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                id,
                "viscript_recipe.editor.category.industrial_foregoing." + id.getPath(),
                MOD_ID,
                REQUIRED_MODS,
                defaultType,
                RecipeEditorLayout.INDUSTRIAL_FOREGOING,
                IndustrialForegoingRecipeEditorTypes.id(workstationPath)
        ));
    }

    private static void register(ResourceLocation id,
                                 Class<? extends IVSRecipeData> dataClass, Supplier<? extends IVSRecipeData> dataSupplier,
                                 BiFunction<NavigationView, RecipeEntry, RecipeCanvas<?>> canvasSupplier) {
        RecipeEditorTypes.register(RecipeEditorType.of(id, id,
                "viscript_recipe.editor.type.industrial_foregoing." + id.getPath(),
                dataClass, dataSupplier, canvasSupplier, MOD_ID
        ));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
