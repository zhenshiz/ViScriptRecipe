package com.viscript_recipe.compat.mysticalagriculture;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.mysticalagriculture.canvas.*;
import com.viscript_recipe.compat.mysticalagriculture.data.*;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = MysticalAgricultureRecipeEditorTypes.MOD_ID, modID = MysticalAgricultureRecipeEditorTypes.MOD_ID)
public final class MysticalAgricultureRecipeEditorTypes implements IModModule{
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

    @Override
    public RecipeImportHandler importHandler() {return MysticalAgricultureRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        registerCategory(INFUSION_ALTAR, INFUSION);
        registerCategory(AWAKENING_ALTAR, AWAKENING);
        registerCategory(ENCHANTER_BLOCK, ENCHANTER);
        registerCategory(REPROCESSOR_BLOCK, REPROCESSOR);
        registerCategory(SOUL_EXTRACTOR_BLOCK, SOUL_EXTRACTION);
        registerCategory(SOULIUM_SPAWNER_BLOCK, SOULIUM_SPAWNER);
        registerTypes();
    }

    private void registerCategory(ResourceLocation category, ResourceLocation type) {
        registerCategory(RecipeEditorCategory.of(
                category, "viscript_recipe.editor.category.mysticalagriculture." + category.getPath(),
                MOD_ID, type, category
        ));
    }

    private void registerTypes() {
        register(INFUSION, INFUSION_ALTAR,
                MysticalAgricultureInfusionRecipeData.class, InfusionCanvas.class);
        register(AWAKENING, AWAKENING_ALTAR,
                MysticalAgricultureAwakeningRecipeData.class, AwakeningCanvas.class);
        register(ENCHANTER, ENCHANTER_BLOCK,
                MysticalAgricultureEnchanterRecipeData.class, EnchanterCanvas.class);
        register(REPROCESSOR, REPROCESSOR_BLOCK,
                MysticalAgricultureReprocessorRecipeData.class, ReprocessorCanvas.class);
        register(SOUL_EXTRACTION, SOUL_EXTRACTOR_BLOCK,
                MysticalAgricultureSoulExtractionRecipeData.class, SoulExtractionCanvas.class);
        register(SOULIUM_SPAWNER, SOULIUM_SPAWNER_BLOCK,
                MysticalAgricultureSouliumSpawnerRecipeData.class, SouliumSpawnerCanvas.class);
    }

    private void register(ResourceLocation id, ResourceLocation category,
            Class<? extends IVSRecipeData> dataClass, Class<? extends RecipeCanvas<?>> canvasClass
    ) {
        registerEditorType(RecipeEditorType.of(id, category,
                "viscript_recipe.editor.type.mysticalagriculture." + id.getPath(),
                dataClass, canvasClass
        ));
    }

    public static ResourceLocation mystical(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
