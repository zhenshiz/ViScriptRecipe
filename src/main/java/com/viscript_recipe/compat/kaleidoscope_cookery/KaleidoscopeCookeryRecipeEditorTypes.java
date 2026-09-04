package com.viscript_recipe.compat.kaleidoscope_cookery;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.kaleidoscope_cookery.canvas.*;
import com.viscript_recipe.compat.kaleidoscope_cookery.data.*;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = KaleidoscopeCookeryRecipeEditorTypes.MOD_ID, modID = KaleidoscopeCookeryRecipeEditorTypes.MOD_ID)
public final class KaleidoscopeCookeryRecipeEditorTypes implements IModModule{
    public static final String MOD_ID = "kaleidoscope_cookery";

    public static final ResourceLocation POT = kaleidoscope("pot");
    public static final ResourceLocation STOCKPOT = kaleidoscope("stockpot");
    public static final ResourceLocation MILLSTONE = kaleidoscope("millstone");
    public static final ResourceLocation CHOPPING_BOARD = kaleidoscope("chopping_board");
    public static final ResourceLocation STEAMER = kaleidoscope("steamer");
    public static final ResourceLocation TEAPOT = kaleidoscope("teapot");

    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return KaleidoscopeCookeryRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategories();
        registerTypes();
    }

    private void registerCategories() {
        registerCategory(POT, "viscript_recipe.editor.category.kaleidoscope_cookery.pot", POT);
        registerCategory(STOCKPOT, "viscript_recipe.editor.category.kaleidoscope_cookery.stockpot", STOCKPOT);
        registerCategory(MILLSTONE, "viscript_recipe.editor.category.kaleidoscope_cookery.millstone", MILLSTONE);
        registerCategory(CHOPPING_BOARD, "viscript_recipe.editor.category.kaleidoscope_cookery.chopping_board", CHOPPING_BOARD);
        registerCategory(STEAMER, "viscript_recipe.editor.category.kaleidoscope_cookery.steamer", STEAMER);
        registerCategory(TEAPOT, "viscript_recipe.editor.category.kaleidoscope_cookery.teapot", TEAPOT);
    }

    private void registerCategory(ResourceLocation category, String translationKey, ResourceLocation defaultType) {
        registerCategory(RecipeEditorCategory.of(category, translationKey, MOD_ID, defaultType, category));
    }

    private void registerTypes() {
        registerEditorType(RecipeEditorType.of(
                POT, POT,
                "viscript_recipe.editor.type.kaleidoscope_cookery.pot",
                KaleidoscopePotRecipeData.class, KaleidoscopePotRecipeData::new,
                PotCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                STOCKPOT, STOCKPOT,
                "viscript_recipe.editor.type.kaleidoscope_cookery.stockpot",
                KaleidoscopeStockpotRecipeData.class, KaleidoscopeStockpotRecipeData::new,
                StockpotCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                MILLSTONE, MILLSTONE,
                "viscript_recipe.editor.type.kaleidoscope_cookery.millstone",
                KaleidoscopeMillstoneRecipeData.class, KaleidoscopeMillstoneRecipeData::new,
                MillstoneCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                CHOPPING_BOARD, CHOPPING_BOARD,
                "viscript_recipe.editor.type.kaleidoscope_cookery.chopping_board",
                KaleidoscopeChoppingBoardRecipeData.class, KaleidoscopeChoppingBoardRecipeData::new,
                ChoppingBoardCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                STEAMER, STEAMER,
                "viscript_recipe.editor.type.kaleidoscope_cookery.steamer",
                KaleidoscopeSteamerRecipeData.class, KaleidoscopeSteamerRecipeData::new,
                SteamerCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                TEAPOT, TEAPOT,
                "viscript_recipe.editor.type.kaleidoscope_cookery.teapot",
                KaleidoscopeTeapotRecipeData.class, KaleidoscopeTeapotRecipeData::new,
                TeapotCanvas::new, MOD_ID
        ));
    }

    public static ResourceLocation kaleidoscope(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
