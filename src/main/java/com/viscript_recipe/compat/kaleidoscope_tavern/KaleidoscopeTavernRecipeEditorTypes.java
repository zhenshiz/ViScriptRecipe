package com.viscript_recipe.compat.kaleidoscope_tavern;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.kaleidoscope_tavern.canvas.BarrelCanvas;
import com.viscript_recipe.compat.kaleidoscope_tavern.canvas.PressingTubCanvas;
import com.viscript_recipe.compat.kaleidoscope_tavern.canvas.ShakerCanvas;
import com.viscript_recipe.compat.kaleidoscope_tavern.data.KaleidoscopeBarrelRecipeData;
import com.viscript_recipe.compat.kaleidoscope_tavern.data.KaleidoscopePressingTubRecipeData;
import com.viscript_recipe.compat.kaleidoscope_tavern.data.KaleidoscopeShakerRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = KaleidoscopeTavernRecipeEditorTypes.MOD_ID, modID = KaleidoscopeTavernRecipeEditorTypes.MOD_ID)
public final class KaleidoscopeTavernRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "kaleidoscope_tavern";

    public static final ResourceLocation BARREL = kaleidoscope("barrel");
    public static final ResourceLocation PRESSING_TUB = kaleidoscope("pressing_tub");
    public static final ResourceLocation SHAKER = kaleidoscope("shaker");

    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return KaleidoscopeTavernRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategories();
        registerTypes();
    }

    private void registerCategories() {
        registerCategory(BARREL, "viscript_recipe.editor.category.kaleidoscope_tavern.barrel", BARREL);
        registerCategory(PRESSING_TUB, "viscript_recipe.editor.category.kaleidoscope_tavern.pressing_tub", PRESSING_TUB);
        registerCategory(SHAKER, "viscript_recipe.editor.category.kaleidoscope_tavern.shaker", SHAKER);
    }

    private void registerCategory(ResourceLocation category, String translationKey, ResourceLocation defaultType) {
        registerCategory(RecipeEditorCategory.of(category, translationKey, MOD_ID, defaultType, category));
    }

    private void registerTypes() {
        registerEditorType(RecipeEditorType.of(
                BARREL, BARREL,
                "viscript_recipe.editor.type.kaleidoscope_tavern.barrel",
                KaleidoscopeBarrelRecipeData.class, KaleidoscopeBarrelRecipeData::new,
                BarrelCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                PRESSING_TUB, PRESSING_TUB,
                "viscript_recipe.editor.type.kaleidoscope_tavern.pressing_tub",
                KaleidoscopePressingTubRecipeData.class, KaleidoscopePressingTubRecipeData::new,
                PressingTubCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                SHAKER, SHAKER,
                "viscript_recipe.editor.type.kaleidoscope_tavern.shaker",
                KaleidoscopeShakerRecipeData.class, KaleidoscopeShakerRecipeData::new,
                ShakerCanvas::new, MOD_ID
        ));
    }

    public static ResourceLocation kaleidoscope(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
