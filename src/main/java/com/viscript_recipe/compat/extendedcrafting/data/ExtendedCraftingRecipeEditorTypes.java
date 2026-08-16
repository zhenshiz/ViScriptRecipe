package com.viscript_recipe.compat.extendedcrafting.data;

import com.viscript_recipe.compat.extendedcrafting.canvas.*;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

public final class ExtendedCraftingRecipeEditorTypes {
    public static final String MOD_ID = "extendedcrafting";

    public static final ResourceLocation CRAFTING_CORE = create("crafting_core");
    public static final ResourceLocation CRAFTING_TABLE = create("crafting_table");
    public static final ResourceLocation BASIC_TABLE = create("basic_table");
    public static final ResourceLocation ADVANCED_TABLE = create("advanced_table");
    public static final ResourceLocation ELITE_TABLE = create("elite_table");
    public static final ResourceLocation ULTIMATE_TABLE = create("ultimate_table");
    public static final ResourceLocation COMPRESSOR = create("compressor");
    public static final ResourceLocation ENDER_CRAFTER = create("ender_crafter");
    public static final ResourceLocation FLUX_CRAFTER = create("flux_crafter");

    public static final ResourceLocation COMBINATION = create("combination");
    public static final ResourceLocation SHAPED_TABLE = create("shaped_table");
    public static final ResourceLocation SHAPELESS_TABLE = create("shapeless_table");
    public static final ResourceLocation BASIC_SHAPED_TABLE = create("basic_shaped_table");
    public static final ResourceLocation BASIC_SHAPELESS_TABLE = create("basic_shapeless_table");
    public static final ResourceLocation ADVANCED_SHAPED_TABLE = create("advanced_shaped_table");
    public static final ResourceLocation ADVANCED_SHAPELESS_TABLE = create("advanced_shapeless_table");
    public static final ResourceLocation ELITE_SHAPED_TABLE = create("elite_shaped_table");
    public static final ResourceLocation ELITE_SHAPELESS_TABLE = create("elite_shapeless_table");
    public static final ResourceLocation ULTIMATE_SHAPED_TABLE = create("ultimate_shaped_table");
    public static final ResourceLocation ULTIMATE_SHAPELESS_TABLE = create("ultimate_shapeless_table");
    public static final ResourceLocation ULTIMATE_SINGULARITY = create("ultimate_singularity");
    public static final ResourceLocation COMPRESSOR_RECIPE = create("compressor_recipe");
    public static final ResourceLocation SHAPED_ENDER_CRAFTER = create("shaped_ender_crafter");
    public static final ResourceLocation SHAPELESS_ENDER_CRAFTER = create("shapeless_ender_crafter");
    public static final ResourceLocation SHAPED_FLUX_CRAFTER = create("shaped_flux_crafter");
    public static final ResourceLocation SHAPELESS_FLUX_CRAFTER = create("shapeless_flux_crafter");

    private static boolean registered;

    private ExtendedCraftingRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        registerCategories();
        registerTypes();
    }

    private static void registerCategories() {
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                CRAFTING_CORE,
                "viscript_recipe.editor.category.extendedcrafting.crafting_core",
                MOD_ID, COMBINATION
        ));
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                CRAFTING_TABLE,
                "viscript_recipe.editor.category.extendedcrafting.crafting_table",
                MOD_ID, SHAPED_TABLE, BASIC_TABLE
        ));
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                COMPRESSOR,
                "viscript_recipe.editor.category.extendedcrafting.compressor",
                MOD_ID, COMPRESSOR_RECIPE
        ));
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                ENDER_CRAFTER,
                "viscript_recipe.editor.category.extendedcrafting.ender_crafter",
                MOD_ID, SHAPED_ENDER_CRAFTER
        ));
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                FLUX_CRAFTER,
                "viscript_recipe.editor.category.extendedcrafting.flux_crafter",
                MOD_ID, SHAPED_FLUX_CRAFTER
        ));
    }

    private static void registerTypes() {
        RecipeEditorTypes.register(RecipeEditorType.of(
                COMBINATION, CRAFTING_CORE,
                "viscript_recipe.editor.type.extendedcrafting.combination",
                ExtendedCraftingCombinationRecipeData.class, ExtendedCraftingCombinationRecipeData::new,
                CombinationCanvas::new, MOD_ID
        ));
        registerTableType(SHAPED_TABLE, "viscript_recipe.editor.type.extendedcrafting.shaped_table");
        registerTableType(SHAPELESS_TABLE, "viscript_recipe.editor.type.extendedcrafting.shapeless_table");
        RecipeEditorTypes.register(RecipeEditorType.of(
                ULTIMATE_SINGULARITY, CRAFTING_TABLE,
                "viscript_recipe.editor.type.extendedcrafting.ultimate_singularity",
                ExtendedCraftingUltimateSingularityRecipeData.class, ExtendedCraftingUltimateSingularityRecipeData::new,
                UltimateSingularityCanvas::new, MOD_ID
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                COMPRESSOR_RECIPE, COMPRESSOR,
                "viscript_recipe.editor.type.extendedcrafting.compressor",
                ExtendedCraftingCompressorRecipeData.class, ExtendedCraftingCompressorRecipeData::new,
                CompressorCanvas::new, MOD_ID
        ));
        registerEnderType(SHAPED_ENDER_CRAFTER, "viscript_recipe.editor.type.extendedcrafting.shaped_ender_crafter");
        registerEnderType(SHAPELESS_ENDER_CRAFTER, "viscript_recipe.editor.type.extendedcrafting.shapeless_ender_crafter");
        registerFluxType(SHAPED_FLUX_CRAFTER, "viscript_recipe.editor.type.extendedcrafting.shaped_flux_crafter");
        registerFluxType(SHAPELESS_FLUX_CRAFTER, "viscript_recipe.editor.type.extendedcrafting.shapeless_flux_crafter");
    }

    private static void registerTableType(ResourceLocation type, String translationKey) {
        RecipeEditorTypes.register(RecipeEditorType.of(
                type, CRAFTING_TABLE, translationKey,
                ExtendedCraftingTableRecipeData.class, ExtendedCraftingTableRecipeData::new,
                CraftingTableCanvas::new, MOD_ID
        ));
    }

    private static void registerEnderType(ResourceLocation type, String translationKey) {
        RecipeEditorTypes.register(RecipeEditorType.of(
                type, ENDER_CRAFTER, translationKey,
                ExtendedCraftingEnderCrafterRecipeData.class, ExtendedCraftingEnderCrafterRecipeData::new,
                EnderCrafterCanvas::new, MOD_ID
        ));
    }

    private static void registerFluxType(ResourceLocation type, String translationKey) {
        RecipeEditorTypes.register(RecipeEditorType.of(
                type, FLUX_CRAFTER, translationKey,
                ExtendedCraftingFluxCrafterRecipeData.class, ExtendedCraftingFluxCrafterRecipeData::new,
                FluxCrafterCanvas::new, MOD_ID
        ));
    }

    public static boolean isTableType(ResourceLocation type) {
        return isShapedTableType(type) || isShapelessTableType(type);
    }

    public static boolean isShapedTableType(ResourceLocation type) {
        return SHAPED_TABLE.equals(type)
                || BASIC_SHAPED_TABLE.equals(type)
                || ADVANCED_SHAPED_TABLE.equals(type)
                || ELITE_SHAPED_TABLE.equals(type)
                || ULTIMATE_SHAPED_TABLE.equals(type);
    }

    public static boolean isShapelessTableType(ResourceLocation type) {
        return SHAPELESS_TABLE.equals(type)
                || BASIC_SHAPELESS_TABLE.equals(type)
                || ADVANCED_SHAPELESS_TABLE.equals(type)
                || ELITE_SHAPELESS_TABLE.equals(type)
                || ULTIMATE_SHAPELESS_TABLE.equals(type);
    }

    public static boolean isShapedEnderType(ResourceLocation type) {
        return SHAPED_ENDER_CRAFTER.equals(type);
    }

    public static boolean isShapedFluxType(ResourceLocation type) {
        return SHAPED_FLUX_CRAFTER.equals(type);
    }

    public static int tableTierForType(ResourceLocation type) {
        if (SHAPED_TABLE.equals(type)
                || SHAPELESS_TABLE.equals(type)
                || BASIC_SHAPED_TABLE.equals(type)
                || BASIC_SHAPELESS_TABLE.equals(type)) {
            return 1;
        }
        if (ADVANCED_SHAPED_TABLE.equals(type) || ADVANCED_SHAPELESS_TABLE.equals(type)) {
            return 2;
        }
        if (ELITE_SHAPED_TABLE.equals(type) || ELITE_SHAPELESS_TABLE.equals(type)) {
            return 3;
        }
        if (ULTIMATE_SHAPED_TABLE.equals(type) || ULTIMATE_SHAPELESS_TABLE.equals(type) || ULTIMATE_SINGULARITY.equals(type)) {
            return 4;
        }
        return 1;
    }

    public static ResourceLocation normalizeAlias(ResourceLocation type) {
        if (BASIC_SHAPED_TABLE.equals(type)
                || ADVANCED_SHAPED_TABLE.equals(type)
                || ELITE_SHAPED_TABLE.equals(type)
                || ULTIMATE_SHAPED_TABLE.equals(type)) {
            return SHAPED_TABLE;
        }
        if (BASIC_SHAPELESS_TABLE.equals(type)
                || ADVANCED_SHAPELESS_TABLE.equals(type)
                || ELITE_SHAPELESS_TABLE.equals(type)
                || ULTIMATE_SHAPELESS_TABLE.equals(type)) {
            return SHAPELESS_TABLE;
        }
        return type;
    }

    public static int tableGridSizeForTier(int tier) {
        return switch (Math.clamp(tier, 1, 4)) {
            case 1 -> 3;
            case 2 -> 5;
            case 3 -> 7;
            default -> 9;
        };
    }

    public static ResourceLocation tableItemForTier(int tier) {
        return switch (Math.clamp(tier, 1, 4)) {
            case 1 -> BASIC_TABLE;
            case 2 -> ADVANCED_TABLE;
            case 3 -> ELITE_TABLE;
            default -> ULTIMATE_TABLE;
        };
    }

    public static ResourceLocation create(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
