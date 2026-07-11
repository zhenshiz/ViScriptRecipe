package com.viscript_recipe.compat;

import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.ars_nouveau.ArsNouveauRecipeImporter;
import com.viscript_recipe.compat.avaritia.AvaritiaRecipeImporter;
import com.viscript_recipe.compat.cataclysm.CataclysmRecipeImporter;
import com.viscript_recipe.compat.create.CreateRecipeImporter;
import com.viscript_recipe.compat.extendedcrafting.ExtendedCraftingRecipeImporter;
import com.viscript_recipe.compat.farmersdelight.FarmersDelightRecipeImporter;
import com.viscript_recipe.compat.goety.GoetyRecipeImporter;
import com.viscript_recipe.compat.iceandfire.IceAndFireRecipeImporter;
import com.viscript_recipe.compat.irons_spellbooks.IronSpellbooksRecipeImporter;
import com.viscript_recipe.compat.kaleidoscope_cookery.KaleidoscopeCookeryRecipeImporter;
import com.viscript_recipe.compat.spore.SporeRecipeImporter;
import com.viscript_recipe.compat.touhou_little_maid.TouhouLittleMaidRecipeImporter;
import com.viscript_recipe.data.ars_nouveau.ArsNouveauRecipeEditorTypes;
import com.viscript_recipe.data.avaritia.AvaritiaRecipeEditorTypes;
import com.viscript_recipe.data.cataclysm.CataclysmRecipeEditorTypes;
import com.viscript_recipe.data.create.CreateRecipeEditorTypes;
import com.viscript_recipe.data.extendedcrafting.ExtendedCraftingRecipeEditorTypes;
import com.viscript_recipe.data.farmersdelight.FarmersDelightRecipeEditorTypes;
import com.viscript_recipe.data.goety.GoetyRecipeEditorTypes;
import com.viscript_recipe.data.iceandfire.IceAndFireRecipeEditorTypes;
import com.viscript_recipe.data.irons_spellbooks.IronSpellbooksRecipeEditorTypes;
import com.viscript_recipe.data.kaleidoscope_cookery.KaleidoscopeCookeryRecipeEditorTypes;
import com.viscript_recipe.data.spore.SporeRecipeEditorTypes;
import com.viscript_recipe.data.touhou_little_maid.TouhouLittleMaidRecipeEditorTypes;
import com.viscript_recipe.data.vanilla.VanillaRecipeEditorTypes;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;

import java.util.List;
import java.util.function.Supplier;

public final class RecipeCompatModules {
    private static final List<Module> MODULES = List.of(
            new Module(IronSpellbooksRecipeEditorTypes.MOD_ID, IronSpellbooksRecipeEditorTypes::registerAll, () -> IronSpellbooksRecipeImporter.INSTANCE),
            new Module(IceAndFireRecipeEditorTypes.MOD_ID, IceAndFireRecipeEditorTypes::registerAll, () -> IceAndFireRecipeImporter.INSTANCE),
            new Module(FarmersDelightRecipeEditorTypes.MOD_ID, FarmersDelightRecipeEditorTypes::registerAll, () -> FarmersDelightRecipeImporter.INSTANCE),
            new Module(CreateRecipeEditorTypes.MOD_ID, CreateRecipeEditorTypes::registerAll, () -> CreateRecipeImporter.INSTANCE),
            new Module(ExtendedCraftingRecipeEditorTypes.MOD_ID, ExtendedCraftingRecipeEditorTypes::registerAll, () -> ExtendedCraftingRecipeImporter.INSTANCE),
            new Module(ArsNouveauRecipeEditorTypes.MOD_ID, ArsNouveauRecipeEditorTypes::registerAll, () -> ArsNouveauRecipeImporter.INSTANCE),
            new Module(KaleidoscopeCookeryRecipeEditorTypes.MOD_ID, KaleidoscopeCookeryRecipeEditorTypes::registerAll, () -> KaleidoscopeCookeryRecipeImporter.INSTANCE),
            new Module(AvaritiaRecipeEditorTypes.MOD_ID, AvaritiaRecipeEditorTypes::registerAll, () -> AvaritiaRecipeImporter.INSTANCE),
            new Module(SporeRecipeEditorTypes.MOD_ID, SporeRecipeEditorTypes::registerAll, () -> SporeRecipeImporter.INSTANCE),
            new Module(CataclysmRecipeEditorTypes.MOD_ID, CataclysmRecipeEditorTypes::registerAll, () -> CataclysmRecipeImporter.INSTANCE),
            new Module(TouhouLittleMaidRecipeEditorTypes.MOD_ID, TouhouLittleMaidRecipeEditorTypes::registerAll, () -> TouhouLittleMaidRecipeImporter.INSTANCE),
            new Module(GoetyRecipeEditorTypes.MOD_ID, GoetyRecipeEditorTypes::registerAll, () -> GoetyRecipeImporter.INSTANCE)
    );

    private RecipeCompatModules() {
    }

    public static void registerEditorTypes() {
        VanillaRecipeEditorTypes.registerAll();
        for (var module : MODULES) {
            if (module.isLoaded()) {
                module.registerEditorTypes();
            }
        }
    }

    public static void addImportHandlers(List<RecipeImportHandler> handlers) {
        for (var module : MODULES) {
            if (module.isLoaded()) {
                handlers.add(module.importHandler());
            }
        }
    }

    private record Module(String modId, Runnable editorTypesRegistrar, Supplier<RecipeImportHandler> importHandlerSupplier) {
        boolean isLoaded() {
            return ViScriptRecipe.isModLoaded(modId);
        }

        void registerEditorTypes() {
            editorTypesRegistrar.run();
        }

        RecipeImportHandler importHandler() {
            return importHandlerSupplier.get();
        }
    }
}
