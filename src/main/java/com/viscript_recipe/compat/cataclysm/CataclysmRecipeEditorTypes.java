package com.viscript_recipe.compat.cataclysm;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.cataclysm.canvas.AmethystBlessCanvas;
import com.viscript_recipe.compat.cataclysm.canvas.WeaponFusionCanvas;
import com.viscript_recipe.compat.cataclysm.data.CataclysmAmethystBlessRecipeData;
import com.viscript_recipe.compat.cataclysm.data.CataclysmWeaponFusionRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = CataclysmRecipeEditorTypes.MOD_ID, modID = CataclysmRecipeEditorTypes.MOD_ID)
public final class CataclysmRecipeEditorTypes implements IModModule{
    public static final String MOD_ID = "cataclysm";

    public static final ResourceLocation MECHANICAL_FUSION_ANVIL = cataclysm("mechanical_fusion_anvil");
    public static final ResourceLocation ALTAR_OF_AMETHYST = cataclysm("altar_of_amethyst");
    public static final ResourceLocation WEAPON_FUSION = cataclysm("weapon_fusion");
    public static final ResourceLocation AMETHYST_BLESS = cataclysm("amethyst_bless");

    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() {return CataclysmRecipeImporter.INSTANCE;}

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategories();
        registerTypes();
    }

    private void registerCategories() {
        registerCategory(RecipeEditorCategory.of(
                MECHANICAL_FUSION_ANVIL,
                "viscript_recipe.editor.category.cataclysm.mechanical_fusion_anvil",
                MOD_ID, WEAPON_FUSION, MECHANICAL_FUSION_ANVIL
        ));
        registerCategory(RecipeEditorCategory.of(
                ALTAR_OF_AMETHYST,
                "viscript_recipe.editor.category.cataclysm.altar_of_amethyst",
                MOD_ID, AMETHYST_BLESS, ALTAR_OF_AMETHYST
        ));
    }

    private void registerTypes() {
        registerEditorType(RecipeEditorType.of(
                WEAPON_FUSION, MECHANICAL_FUSION_ANVIL,
                "viscript_recipe.editor.type.cataclysm.weapon_fusion",
                CataclysmWeaponFusionRecipeData.class, CataclysmWeaponFusionRecipeData::new,
                WeaponFusionCanvas::new, MOD_ID
        ));
        registerEditorType(RecipeEditorType.of(
                AMETHYST_BLESS, ALTAR_OF_AMETHYST,
                "viscript_recipe.editor.type.cataclysm.amethyst_bless",
                CataclysmAmethystBlessRecipeData.class, CataclysmAmethystBlessRecipeData::new,
                AmethystBlessCanvas::new, MOD_ID
        ));
    }

    public static ResourceLocation cataclysm(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
