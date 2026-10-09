package com.viscript_recipe.compat.ae2;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.ae2.canvas.ChargerCanvas;
import com.viscript_recipe.compat.ae2.canvas.EntropyCanvas;
import com.viscript_recipe.compat.ae2.canvas.InscriberCanvas;
import com.viscript_recipe.compat.ae2.canvas.TransformCanvas;
import com.viscript_recipe.compat.ae2.data.Ae2ChargerRecipeData;
import com.viscript_recipe.compat.ae2.data.Ae2EntropyRecipeData;
import com.viscript_recipe.compat.ae2.data.Ae2InscriberRecipeData;
import com.viscript_recipe.compat.ae2.data.Ae2TransformRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = Ae2RecipeEditorTypes.MOD_ID, modID = Ae2RecipeEditorTypes.MOD_ID)
public final class Ae2RecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "ae2";
    public static final ResourceLocation INSCRIBER = id("inscriber");
    public static final ResourceLocation CHARGER = id("charger");
    public static final ResourceLocation TRANSFORM = id("transform");
    public static final ResourceLocation ENTROPY = id("entropy");

    @Override
    public RecipeImportHandler importHandler() { return Ae2RecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        category(INSCRIBER, "inscriber");
        category(CHARGER, "charger");
        category(TRANSFORM, "fluix_crystal");
        category(ENTROPY, "entropy_manipulator");
        registerEditorType(RecipeEditorType.of(INSCRIBER, INSCRIBER, key(INSCRIBER),
                Ae2InscriberRecipeData.class, InscriberCanvas.class));
        registerEditorType(RecipeEditorType.of(CHARGER, CHARGER, key(CHARGER),
                Ae2ChargerRecipeData.class, ChargerCanvas.class));
        registerEditorType(RecipeEditorType.of(TRANSFORM, TRANSFORM, key(TRANSFORM),
                Ae2TransformRecipeData.class, TransformCanvas.class));
        registerEditorType(RecipeEditorType.of(ENTROPY, ENTROPY, key(ENTROPY),
                Ae2EntropyRecipeData.class, EntropyCanvas.class));
    }

    private void category(ResourceLocation type, String icon) {
        registerCategory(RecipeEditorCategory.of(type, key(type), MOD_ID, type, id(icon)));
    }

    private static String key(ResourceLocation type) {
        return "viscript_recipe.editor.type.ae2." + type.getPath();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
