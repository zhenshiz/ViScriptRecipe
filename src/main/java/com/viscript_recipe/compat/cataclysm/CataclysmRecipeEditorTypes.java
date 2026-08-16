package com.viscript_recipe.compat.cataclysm;

import com.viscript_recipe.compat.cataclysm.canvas.AmethystBlessCanvas;
import com.viscript_recipe.compat.cataclysm.canvas.WeaponFusionCanvas;
import com.viscript_recipe.compat.cataclysm.data.CataclysmAmethystBlessRecipeData;
import com.viscript_recipe.compat.cataclysm.data.CataclysmWeaponFusionRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import net.minecraft.resources.ResourceLocation;

public final class CataclysmRecipeEditorTypes {
    public static final String MOD_ID = "cataclysm";

    public static final ResourceLocation MECHANICAL_FUSION_ANVIL = cataclysm("mechanical_fusion_anvil");
    public static final ResourceLocation ALTAR_OF_AMETHYST = cataclysm("altar_of_amethyst");
    public static final ResourceLocation WEAPON_FUSION = cataclysm("weapon_fusion");
    public static final ResourceLocation AMETHYST_BLESS = cataclysm("amethyst_bless");

    private static boolean registered;

    private CataclysmRecipeEditorTypes() {
    }

    public static synchronized void registerAll() {
        if (registered) {
            return;
        }
        registered = true;
        registerCategories();
        registerTypes();
    }

    private static void registerCategories() {
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                MECHANICAL_FUSION_ANVIL,
                "viscript_recipe.editor.category.cataclysm.mechanical_fusion_anvil",
                MOD_ID, WEAPON_FUSION, MECHANICAL_FUSION_ANVIL
        ));
        RecipeEditorTypes.registerCategory(RecipeEditorCategory.of(
                ALTAR_OF_AMETHYST,
                "viscript_recipe.editor.category.cataclysm.altar_of_amethyst",
                MOD_ID, AMETHYST_BLESS, ALTAR_OF_AMETHYST
        ));
    }

    private static void registerTypes() {
        RecipeEditorTypes.register(RecipeEditorType.of(
                WEAPON_FUSION, MECHANICAL_FUSION_ANVIL,
                "viscript_recipe.editor.type.cataclysm.weapon_fusion",
                CataclysmWeaponFusionRecipeData.class, CataclysmWeaponFusionRecipeData::new,
                WeaponFusionCanvas::new, MOD_ID
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
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
