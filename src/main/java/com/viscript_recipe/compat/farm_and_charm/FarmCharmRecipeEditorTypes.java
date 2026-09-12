package com.viscript_recipe.compat.farm_and_charm;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.farm_and_charm.canvas.FarmCharmCanvas;
import com.viscript_recipe.compat.farm_and_charm.data.FarmCharmRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;

/** Registers the six native JEI recipe editors only when Farm & Charm is installed. */
@LDLRegister(registry = IModModule.ID, name = FarmCharmRecipeKind.MOD_ID, modID = FarmCharmRecipeKind.MOD_ID)
public final class FarmCharmRecipeEditorTypes implements IModModule {
    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() { return FarmCharmRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        for (var kind : FarmCharmRecipeKind.values()) {
            registerCategory(RecipeEditorCategory.of(kind.typeId(), kind.translationKey(), FarmCharmRecipeKind.MOD_ID,
                    kind.typeId(), kind.workstationId()));
            registerEditorType(RecipeEditorType.of(kind.typeId(), kind.typeId(), kind.translationKey(),
                    FarmCharmRecipeData.class, FarmCharmRecipeData::new, FarmCharmCanvas::new, FarmCharmRecipeKind.MOD_ID));
        }
    }
}
