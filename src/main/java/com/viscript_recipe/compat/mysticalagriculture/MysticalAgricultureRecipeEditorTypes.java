package com.viscript_recipe.compat.mysticalagriculture;

import com.viscript_recipe.compat.mysticalagriculture.canvas.*;
import com.viscript_recipe.compat.mysticalagriculture.data.*;
import com.viscript_recipe.data.*;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public final class MysticalAgricultureRecipeEditorTypes {
    public static final String MOD_ID = "mysticalagriculture";

    public static final ResourceLocation INFUSION_ALTAR = mystical("infusion_altar");
    public static final ResourceLocation AWAKENING_ALTAR = mystical("awakening_altar");
    public static final ResourceLocation ENCHANTER_BLOCK = mystical("enchanter");
    public static final ResourceLocation REPROCESSOR_BLOCK = mystical("seed_reprocessor");
    public static final ResourceLocation SOUL_EXTRACTOR_BLOCK = mystical("soul_extractor");
    public static final ResourceLocation SOULIUM_SPAWNER_BLOCK = mystical("soulium_spawner");

    public static final ResourceLocation INFUSION = mystical("infusion");
    public static final ResourceLocation AWAKENING = mystical("awakening");
    public static final ResourceLocation ENCHANTER = mystical("enchanter");
    public static final ResourceLocation REPROCESSOR = mystical("reprocessor");
    public static final ResourceLocation SOUL_EXTRACTION = mystical("soul_extraction");
    public static final ResourceLocation SOULIUM_SPAWNER = mystical("soulium_spawner");

    private static boolean registered;

    private MysticalAgricultureRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        registerCategory(INFUSION_ALTAR, INFUSION);
        registerCategory(AWAKENING_ALTAR, AWAKENING);
        registerCategory(ENCHANTER_BLOCK, ENCHANTER);
        registerCategory(REPROCESSOR_BLOCK, REPROCESSOR);
        registerCategory(SOUL_EXTRACTOR_BLOCK, SOUL_EXTRACTION);
        registerCategory(SOULIUM_SPAWNER_BLOCK, SOULIUM_SPAWNER);
        registerTypes();
    }

    private static void registerCategory(ResourceLocation category, ResourceLocation type) {
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                category, "viscript_recipe.editor.category.mysticalagriculture." + category.getPath(),
                MOD_ID, type, category
        ));
    }

    private static void registerTypes() {
        register(INFUSION, INFUSION_ALTAR,
                MysticalAgricultureInfusionRecipeData.class, MysticalAgricultureInfusionRecipeData::new, InfusionCanvas::new);
        register(AWAKENING, AWAKENING_ALTAR,
                MysticalAgricultureAwakeningRecipeData.class, MysticalAgricultureAwakeningRecipeData::new, AwakeningCanvas::new);
        register(ENCHANTER, ENCHANTER_BLOCK,
                MysticalAgricultureEnchanterRecipeData.class, MysticalAgricultureEnchanterRecipeData::new, EnchanterCanvas::new);
        register(REPROCESSOR, REPROCESSOR_BLOCK,
                MysticalAgricultureReprocessorRecipeData.class, MysticalAgricultureReprocessorRecipeData::new, ReprocessorCanvas::new);
        register(SOUL_EXTRACTION, SOUL_EXTRACTOR_BLOCK,
                MysticalAgricultureSoulExtractionRecipeData.class, MysticalAgricultureSoulExtractionRecipeData::new, SoulExtractionCanvas::new);
        register(SOULIUM_SPAWNER, SOULIUM_SPAWNER_BLOCK,
                MysticalAgricultureSouliumSpawnerRecipeData.class, MysticalAgricultureSouliumSpawnerRecipeData::new, SouliumSpawnerCanvas::new);
    }

    private static void register(ResourceLocation id, ResourceLocation category,
            Class<? extends IVSRecipeData> dataClass, Supplier<? extends IVSRecipeData> dataSupplier,
            java.util.function.BiFunction<com.viscript_recipe.gui.views.NavigationView, RecipeEntry,
                    com.viscript_recipe.gui.canvas.RecipeCanvas<?>> canvasSupplier
    ) {
        RecipeEditorTypes.register(RecipeEditorType.of(id, category,
                "viscript_recipe.editor.type.mysticalagriculture." + id.getPath(),
                dataClass, dataSupplier, canvasSupplier, MOD_ID
        ));
    }

    public static ResourceLocation mystical(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
