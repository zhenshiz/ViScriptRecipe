package com.viscript_recipe.compat.forbidden_arcanus;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.forbidden_arcanus.canvas.ApplyModifierCanvas;
import com.viscript_recipe.compat.forbidden_arcanus.canvas.ClibanoCanvas;
import com.viscript_recipe.compat.forbidden_arcanus.canvas.ForgeRitualCanvas;
import com.viscript_recipe.compat.forbidden_arcanus.data.ApplyModifierRecipeData;
import com.viscript_recipe.compat.forbidden_arcanus.data.ClibanoRecipeData;
import com.viscript_recipe.compat.forbidden_arcanus.data.ForgeRitualRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;

@LDLRegister(registry = IModModule.ID, name = ForbiddenArcanusRecipeEditorTypes.MOD_ID,
        modID = ForbiddenArcanusRecipeEditorTypes.MOD_ID)
public final class ForbiddenArcanusRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "forbidden_arcanus";
    public static final ResourceLocation HEPHAESTUS_FORGE = id("hephaestus_forge");
    public static final ResourceLocation HEPHAESTUS_SMITHING = id("hephaestus_smithing");
    public static final ResourceLocation HEPHAESTUS_UPGRADING = id("hephaestus_forge_upgrading");
    public static final ResourceLocation CLIBANO_CORE = id("clibano_core");
    public static final ResourceLocation CLIBANO_COMBUSTION = id("clibano_combustion");
    public static final ResourceLocation APPLY_MODIFIER = id("apply_modifier");

    @Override
    public RecipeImportHandler importHandler() {return ForbiddenArcanusRecipeImporter.INSTANCE;}

    @Override
    public void registerRuntime(IEventBus modEventBus) {ForgeRitualRecipes.register(modEventBus);}

    @Override
    public void registerEditorTypes() {
        registerCategory(RecipeEditorCategory.of(HEPHAESTUS_FORGE, "viscript_recipe.editor.category.forbidden_arcanus.hephaestus_forge",
                MOD_ID, HEPHAESTUS_SMITHING, id("hephaestus_forge_tier_1")));
        registerCategory(RecipeEditorCategory.of(CLIBANO_CORE, "viscript_recipe.editor.category.forbidden_arcanus.clibano",
                MOD_ID, CLIBANO_COMBUSTION));
        registerEditorType(RecipeEditorType.of(HEPHAESTUS_SMITHING, HEPHAESTUS_FORGE,
                "viscript_recipe.editor.type.forbidden_arcanus.hephaestus_smithing", ForgeRitualRecipeData.class, ForgeRitualCanvas.class));
        registerEditorType(RecipeEditorType.of(HEPHAESTUS_UPGRADING, HEPHAESTUS_FORGE,
                "viscript_recipe.editor.type.forbidden_arcanus.hephaestus_forge_upgrading", ForgeRitualRecipeData.class, ForgeRitualCanvas.class));
        registerEditorType(RecipeEditorType.of(CLIBANO_COMBUSTION, CLIBANO_CORE,
                "viscript_recipe.editor.type.forbidden_arcanus.clibano_combustion", ClibanoRecipeData.class, ClibanoCanvas.class));
        registerEditorType(RecipeEditorType.of(APPLY_MODIFIER, RecipeEditorTypes.SMITHING_TABLE,
                "viscript_recipe.editor.type.forbidden_arcanus.apply_modifier", ApplyModifierRecipeData.class, ApplyModifierCanvas.class));
    }

    public static ResourceLocation id(String path) {return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);}
}
