package com.viscript_recipe.compat.lychee;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.compat.lychee.canvas.LycheeRecipeCanvas;
import com.viscript_recipe.compat.lychee.data.LycheeRecipeData;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** 注册 Lychee 原生配方的编辑入口，执行与配方展示由 Lychee 负责。 */
@LDLRegister(registry = IModModule.ID, name = LycheeRecipeEditorTypes.MOD_ID, modID = LycheeRecipeEditorTypes.MOD_ID)
public final class LycheeRecipeEditorTypes implements IModModule {
    public static final String MOD_ID = "lychee";
    public static final List<String> TYPES = List.of("block_interacting", "block_clicking", "item_burning", "item_inside",
            "anvil_crafting", "block_crushing", "lightning_channeling", "item_exploding", "block_exploding",
            "random_block_ticking", "dripstone_dripping", "crafting", "entity_ticking", "blank");

    @Override
    public RecipeImportHandler importHandler() { return LycheeRecipeImporter.INSTANCE; }

    @Override
    public void registerEditorTypes() {
        for (String type : TYPES) {
            String icon = switch (type) {
                case "anvil_crafting", "block_crushing" -> "anvil";
                case "item_burning" -> "flint_and_steel";
                case "item_inside" -> "water_bucket";
                case "lightning_channeling" -> "lightning_rod";
                case "item_exploding", "block_exploding" -> "tnt";
                case "random_block_ticking" -> "grass_block";
                case "dripstone_dripping" -> "pointed_dripstone";
                case "entity_ticking" -> "pig_spawn_egg";
                case "blank" -> "knowledge_book";
                default -> "crafting_table";
            };
            registerCategory(RecipeEditorCategory.of(id(type), key(type), MOD_ID, id(type), ResourceLocation.withDefaultNamespace(icon)));
            registerEditorType(RecipeEditorType.of(id(type), id(type), key(type), LycheeRecipeData.class, LycheeRecipeCanvas.class));
        }
    }

    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(MOD_ID, path); }
    public static String key(String path) { return "viscript_recipe.editor.lychee." + path; }
}
