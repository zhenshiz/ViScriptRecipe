package com.viscript_recipe.compat.bakery;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.bakery.canvas.BakeryBakingStationCanvas;
import com.viscript_recipe.compat.bakery.canvas.BakeryCakeInteractionCanvas;
import com.viscript_recipe.compat.bakery.data.BakeryBakingStationRecipeData;
import com.viscript_recipe.compat.bakery.data.BakeryCakeInteractionRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

/** 注册 Bakery 自有烘焙及蛋糕交互配方，不重复注册 Farm & Charm 的配方。 */
@LDLRegister(registry = IModModule.ID, name = BakeryRecipeEditorTypes.MOD_ID, modID = BakeryRecipeEditorTypes.MOD_ID)
public final class BakeryRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "bakery";
    public static final ResourceLocation BAKING_STATION = ResourceLocation.fromNamespaceAndPath(MOD_ID, "baking_station");
    public static final String NAME_KEY = "block.bakery.baker_station";
    public static final ResourceLocation CAKE_INTERACTION = ResourceLocation.fromNamespaceAndPath(MOD_ID, "blank_cake_interaction");
    public static final String INTERACTION_KEY = "viscript_recipe.editor.bakery.cake_interaction";
    private static boolean registered;

    @Override
    public RecipeImportHandler importHandler() { return BakeryRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        if (registered) return;
        registered = true;
        registerCategory(RecipeEditorCategory.of(BAKING_STATION, NAME_KEY, MOD_ID, BAKING_STATION,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "baker_station")));
        registerEditorType(RecipeEditorType.of(BAKING_STATION, BAKING_STATION, NAME_KEY,
                BakeryBakingStationRecipeData.class, BakeryBakingStationRecipeData::new, BakeryBakingStationCanvas::new,
                MOD_ID, "farm_and_charm"));
        registerCategory(RecipeEditorCategory.of(CAKE_INTERACTION, INTERACTION_KEY, MOD_ID, CAKE_INTERACTION,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "baker_station")));
        registerEditorType(RecipeEditorType.of(CAKE_INTERACTION, CAKE_INTERACTION, INTERACTION_KEY,
                BakeryCakeInteractionRecipeData.class, BakeryCakeInteractionRecipeData::new, BakeryCakeInteractionCanvas::new,
                MOD_ID, "farm_and_charm"));
    }
}
