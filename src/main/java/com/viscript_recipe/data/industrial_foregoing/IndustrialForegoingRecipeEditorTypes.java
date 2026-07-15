package com.viscript_recipe.data.industrial_foregoing;

import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorLayout;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Registers the codec-backed Industrial Foregoing recipe types exposed by the editor. */
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

    /** Registers Industrial Foregoing categories and editor types once. */
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

        RecipeEditorTypes.register(type(CRUSHER,
                entry -> entry.getIndustrialCrusher().compile(),
                entry -> firstStack(entry.getIndustrialCrusher().getOutput()),
                (entry, stack) -> entry.getIndustrialCrusher().setOutput(com.viscript_recipe.data.RecipeIngredient.item(stack))));
        RecipeEditorTypes.register(type(DISSOLUTION_CHAMBER,
                entry -> entry.getIndustrialDissolution().compile(),
                entry -> entry.getIndustrialDissolution().isHasItemOutput() ? entry.getIndustrialDissolution().getOutput() : ItemStack.EMPTY,
                (entry, stack) -> entry.getIndustrialDissolution().setHasItemOutput(!stack.isEmpty()).setOutput(stack.copy())));
        RecipeEditorTypes.register(type(FLUID_EXTRACTOR,
                entry -> entry.getIndustrialFluidExtractor().compile(),
                entry -> ItemStack.EMPTY,
                (entry, stack) -> { }));
        RecipeEditorTypes.register(type(LASER_DRILL_ORE,
                entry -> entry.getIndustrialLaserDrillOre().compile(),
                entry -> firstStack(entry.getIndustrialLaserDrillOre().getOutput()),
                (entry, stack) -> entry.getIndustrialLaserDrillOre().setOutput(com.viscript_recipe.data.RecipeIngredient.item(stack))));
        RecipeEditorTypes.register(type(LASER_DRILL_FLUID,
                entry -> entry.getIndustrialLaserDrillFluid().compile(),
                entry -> ItemStack.EMPTY,
                (entry, stack) -> { }));
        RecipeEditorTypes.register(type(STONEWORK_GENERATE,
                entry -> entry.getIndustrialStoneWork().compile(),
                entry -> entry.getIndustrialStoneWork().getOutput(),
                (entry, stack) -> entry.getIndustrialStoneWork().setOutput(stack.copy())));
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

    private static RecipeEditorType type(ResourceLocation id,
                                         java.util.function.Function<com.viscript_recipe.data.RecipeEntry, net.minecraft.world.item.crafting.Recipe<?>> compiler,
                                         java.util.function.Function<com.viscript_recipe.data.RecipeEntry, ItemStack> getter,
                                         java.util.function.BiConsumer<com.viscript_recipe.data.RecipeEntry, ItemStack> setter) {
        return new RecipeEditorType(
                id, id, "viscript_recipe.editor.type.industrial_foregoing." + id.getPath(), REQUIRED_MODS, false,
                compiler, entry -> false, (entry, value) -> { }, getter, setter
        );
    }

    private static ItemStack firstStack(com.viscript_recipe.data.RecipeIngredient ingredient) {
        if (ingredient == null || ingredient.getValues() == null || ingredient.getValues().isEmpty()) {
            return ItemStack.EMPTY;
        }
        var stacks = ingredient.compile().getItems();
        return stacks.length == 0 ? ItemStack.EMPTY : stacks[0].copy();
    }

    /** Creates an Industrial Foregoing resource identifier. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
