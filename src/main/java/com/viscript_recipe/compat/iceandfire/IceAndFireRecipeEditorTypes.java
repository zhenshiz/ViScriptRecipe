package com.viscript_recipe.compat.iceandfire;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.iceandfire.canvas.DragonForgeCanvas;
import com.viscript_recipe.compat.iceandfire.data.DragonForgeRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = IceAndFireRecipeEditorTypes.MOD_ID, modID = IceAndFireRecipeEditorTypes.MOD_ID)
public final class IceAndFireRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "iceandfire";

    public static final ResourceLocation DRAGON_FORGE = iceandfire("dragon_forge");
    public static final ResourceLocation DRAGONFORGE = iceandfire("dragonforge");

    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return IceAndFireRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategories();
        registerTypes();
    }

    private void registerCategories() {
        registerCategory(RecipeEditorCategory.of(
                DRAGON_FORGE,
                "viscript_recipe.editor.category.iceandfire.dragon_forge",
                MOD_ID, DRAGONFORGE, iceandfire("dragonforge_fire_core")
        ));
    }

    private void registerTypes() {
        registerEditorType(RecipeEditorType.of(
                DRAGONFORGE, DRAGON_FORGE,
                "viscript_recipe.editor.type.iceandfire.dragonforge",
                DragonForgeRecipeData.class, DragonForgeRecipeData::new,
                DragonForgeCanvas::new, MOD_ID
        ));
    }

    private static ResourceLocation iceandfire(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
