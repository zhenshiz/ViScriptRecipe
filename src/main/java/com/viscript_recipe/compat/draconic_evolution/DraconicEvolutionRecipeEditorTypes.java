package com.viscript_recipe.compat.draconic_evolution;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.draconic_evolution.canvas.FusionCraftingCanvas;
import com.viscript_recipe.compat.draconic_evolution.data.DraconicFusionRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = DraconicEvolutionRecipeEditorTypes.MOD_ID,
        modID = DraconicEvolutionRecipeEditorTypes.MOD_ID)
public final class DraconicEvolutionRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "draconicevolution";
    public static final ResourceLocation FUSION_CRAFTING = id("fusion_crafting");
    public static final ResourceLocation CRAFTING_CORE = id("crafting_core");
    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() { return DraconicEvolutionRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategory(RecipeEditorCategory.of(CRAFTING_CORE,
                "viscript_recipe.editor.category.draconicevolution.crafting_core",
                MOD_ID, FUSION_CRAFTING, CRAFTING_CORE));
        registerEditorType(RecipeEditorType.of(FUSION_CRAFTING, CRAFTING_CORE,
                "viscript_recipe.editor.type.draconicevolution.fusion_crafting",
                DraconicFusionRecipeData.class, DraconicFusionRecipeData::new,
                FusionCraftingCanvas::new, MOD_ID));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
