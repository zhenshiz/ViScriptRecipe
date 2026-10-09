package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.gui.canvas.vanilla.*;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import net.minecraft.resources.ResourceLocation;

@LDLRegister(registry = IModModule.ID, name = VanillaRecipeEditorTypes.MOD_ID, priority = 1000)
public final class VanillaRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "minecraft";

    public static final ResourceLocation CRAFTING_TABLE = minecraft("crafting_table");
    public static final ResourceLocation FURNACE = minecraft("furnace");
    public static final ResourceLocation BLAST_FURNACE = minecraft("blast_furnace");
    public static final ResourceLocation SMOKER = minecraft("smoker");
    public static final ResourceLocation CAMPFIRE = minecraft("campfire");
    public static final ResourceLocation STONECUTTER = minecraft("stonecutter");
    public static final ResourceLocation SMITHING_TABLE = minecraft("smithing_table");
    public static final ResourceLocation CRAFTING_SHAPED = minecraft("crafting_shaped");
    public static final ResourceLocation CRAFTING_SHAPELESS = minecraft("crafting_shapeless");
    public static final ResourceLocation SMELTING = minecraft("smelting");
    public static final ResourceLocation BLASTING = minecraft("blasting");
    public static final ResourceLocation SMOKING = minecraft("smoking");
    public static final ResourceLocation CAMPFIRE_COOKING = minecraft("campfire_cooking");
    public static final ResourceLocation STONECUTTING = minecraft("stonecutting");
    public static final ResourceLocation SMITHING_TRANSFORM = minecraft("smithing_transform");

    @Override
    public void registerEditorTypes() {
        registerCategories();
        registerTypes();
    }

    @Override
    public RecipeImportHandler importHandler() {return RecipeImporter.VANILLA_HANDLER;}

    private void registerCategories() {
        registerCategory(new RecipeEditorCategory(
                CRAFTING_TABLE, "viscript_recipe.editor.category.minecraft.crafting_table",
                MOD_ID, CRAFTING_SHAPED
        ));
        registerCategory(new RecipeEditorCategory(
                FURNACE, "viscript_recipe.editor.category.minecraft.furnace",
                MOD_ID, SMELTING
        ));
        registerCategory(new RecipeEditorCategory(
                BLAST_FURNACE, "viscript_recipe.editor.category.minecraft.blast_furnace",
                MOD_ID, BLASTING
        ));
        registerCategory(new RecipeEditorCategory(
                SMOKER, "viscript_recipe.editor.category.minecraft.smoker",
                MOD_ID, SMOKING
        ));
        registerCategory(new RecipeEditorCategory(
                CAMPFIRE, "viscript_recipe.editor.category.minecraft.campfire",
                MOD_ID, CAMPFIRE_COOKING
        ));
        registerCategory(new RecipeEditorCategory(
                STONECUTTER, "viscript_recipe.editor.category.minecraft.stonecutter",
                MOD_ID, STONECUTTING
        ));
        registerCategory(new RecipeEditorCategory(
                SMITHING_TABLE, "viscript_recipe.editor.category.minecraft.smithing_table",
                MOD_ID, SMITHING_TRANSFORM
        ));
    }

    private void registerTypes() {
        registerEditorType(RecipeEditorType.of(
                CRAFTING_SHAPED, CRAFTING_TABLE,
                "viscript_recipe.editor.type.minecraft.crafting_shaped",
                ShapedCraftingRecipeData.class, ShapedCraftingCanvas.class
        ));
        registerEditorType(RecipeEditorType.of(
                CRAFTING_SHAPELESS, CRAFTING_TABLE,
                "viscript_recipe.editor.type.minecraft.crafting_shapeless",
                ShapelessCraftingRecipeData.class, ShapelessCraftingCanvas.class
        ));
        registerEditorType(RecipeEditorType.of(
                SMELTING, FURNACE,
                "viscript_recipe.editor.type.minecraft.smelting",
                CookingRecipeData.class, CookingCanvas.class
        ));
        registerEditorType(RecipeEditorType.of(
                BLASTING, BLAST_FURNACE,
                "viscript_recipe.editor.type.minecraft.blasting",
                CookingRecipeData.class, CookingCanvas.class
        ));
        registerEditorType(RecipeEditorType.of(
                SMOKING, SMOKER,
                "viscript_recipe.editor.type.minecraft.smoking",
                CookingRecipeData.class, CookingCanvas.class
        ));
        registerEditorType(RecipeEditorType.of(
                CAMPFIRE_COOKING, CAMPFIRE,
                "viscript_recipe.editor.type.minecraft.campfire_cooking",
                CookingRecipeData.class, CookingCanvas.class
        ));
        registerEditorType(RecipeEditorType.of(
                STONECUTTING, STONECUTTER,
                "viscript_recipe.editor.type.minecraft.stonecutting",
                StonecuttingRecipeData.class, StonecuttingCanvas.class
        ));
        registerEditorType(RecipeEditorType.of(
                SMITHING_TRANSFORM, SMITHING_TABLE,
                "viscript_recipe.editor.type.minecraft.smithing_transform",
                SmithingTransformRecipeData.class, SmithingTransformCanvas.class
        ));
    }

    private static ResourceLocation minecraft(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
