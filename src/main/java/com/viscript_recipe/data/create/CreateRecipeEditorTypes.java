package com.viscript_recipe.data.create;

import com.viscript_recipe.data.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class CreateRecipeEditorTypes {
    public static final String MOD_ID = "create";
    public static final ResourceLocation MECHANICAL_CRAFTER = create("mechanical_crafter");
    public static final ResourceLocation MECHANICAL_CRAFTING = create("mechanical_crafting");
    public static final ResourceLocation SEQUENCED_ASSEMBLY = create("sequenced_assembly");

    private static final List<String> REQUIRED_MODS = List.of(MOD_ID);
    private static boolean registered;

    private CreateRecipeEditorTypes() {
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
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                MECHANICAL_CRAFTER,
                "viscript_recipe.editor.category.create.mechanical_crafter",
                MOD_ID,
                REQUIRED_MODS,
                MECHANICAL_CRAFTING,
                RecipeEditorLayout.CREATE_MECHANICAL_CRAFTING,
                MECHANICAL_CRAFTER
        ));
        RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                SEQUENCED_ASSEMBLY,
                "create.recipe.sequenced_assembly",
                MOD_ID,
                REQUIRED_MODS,
                SEQUENCED_ASSEMBLY,
                RecipeEditorLayout.CREATE_SEQUENCED_ASSEMBLY,
                null
        ));
        for (var kind : CreateProcessingKind.values()) {
            if (RecipeEditorTypes.getCategory(kind.categoryId()).isPresent()) {
                continue;
            }
            RecipeEditorTypes.registerCategory(new RecipeEditorCategory(
                    kind.categoryId(),
                    categoryFallbackTranslationKey(kind),
                    MOD_ID,
                    REQUIRED_MODS,
                    kind.typeId(),
                    RecipeEditorLayout.CREATE_PROCESSING,
                    categoryWorkstationItem(kind)
            ));
        }
    }

    private static String categoryFallbackTranslationKey(CreateProcessingKind kind) {
        return kind == CreateProcessingKind.ITEM_APPLICATION
                ? "create.recipe.item_application"
                : "viscript_recipe.editor.category.create." + kind.categoryId().getPath();
    }

    private static ResourceLocation categoryWorkstationItem(CreateProcessingKind kind) {
        return kind == CreateProcessingKind.ITEM_APPLICATION ? null : kind.machineItemLocation();
    }

    private static void registerTypes() {
        RecipeEditorTypes.register(RecipeEditorType.of(
                MECHANICAL_CRAFTING, MECHANICAL_CRAFTER,
                "viscript_recipe.editor.type.create.mechanical_crafting",
                CreateMechanicalCraftingRecipeData.class, CreateMechanicalCraftingRecipeData::new,
                MOD_ID
        ));
        RecipeEditorTypes.register(RecipeEditorType.of(
                SEQUENCED_ASSEMBLY, SEQUENCED_ASSEMBLY,
                "viscript_recipe.editor.type.create.sequenced_assembly",
                CreateSequencedAssemblyRecipeData.class, CreateSequencedAssemblyRecipeData::new,
                MOD_ID
        ));
        for (var kind : CreateProcessingKind.values()) {
            RecipeEditorTypes.register(RecipeEditorType.of(
                    kind.typeId(), kind.categoryId(),
                    "viscript_recipe.editor.type.create." + kind.translationPath(),
                    CreateProcessingRecipeData.class, CreateProcessingRecipeData::new,
                    MOD_ID
            ));
        }
    }

    static ItemStack firstOutput(CreateProcessingRecipeData data) {
        if (data.getOutputs() == null || data.getOutputs().isEmpty()) {
            return ItemStack.EMPTY;
        }
        var output = data.getOutputs().getFirst();
        return output == null || output.getItem() == null ? ItemStack.EMPTY : output.getItem();
    }

    static void setFirstOutput(CreateProcessingRecipeData data, ItemStack stack) {
        if (data.getOutputs() == null) {
            data.setOutputs(new java.util.ArrayList<>());
        }
        if (data.getOutputs().isEmpty()) {
            data.getOutputs().add(RecipeOutputData.of());
        }
        data.getOutputs().getFirst().setItem(stack == null ? ItemStack.EMPTY : stack.copy());
    }

    static ItemStack firstOutput(CreateSequencedAssemblyRecipeData data) {
        if (data.getOutputs() == null || data.getOutputs().isEmpty()) {
            return ItemStack.EMPTY;
        }
        var output = data.getOutputs().getFirst();
        return output == null || output.getItem() == null ? ItemStack.EMPTY : output.getItem();
    }

    static void setFirstOutput(CreateSequencedAssemblyRecipeData data, ItemStack stack) {
        if (data.getOutputs() == null) {
            data.setOutputs(new java.util.ArrayList<>());
        }
        if (data.getOutputs().isEmpty()) {
            data.getOutputs().add(RecipeOutputData.of());
        }
        data.getOutputs().getFirst().setItem(stack == null ? ItemStack.EMPTY : stack.copy());
    }

    public static ResourceLocation create(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
