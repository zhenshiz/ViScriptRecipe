package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.viscript_recipe.IModModule;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.RecipeEditorCategory;
import com.viscript_recipe.data.RecipeEditorType;
import com.viscript_recipe.gui.canvas.vanilla.VanillaEventRecipeCanvas;
import com.viscript_recipe.recipe.importer.RecipeImportHandler;
import com.viscript_recipe.recipe.importer.RecipeImportResult;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import com.viscript_recipe.recipe.vanilla.VanillaEventRecipe;
import com.viscript_recipe.recipe.vanilla.VanillaEventRecipeRuntime;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.IEventBus;

@LDLRegister(registry = IModModule.ID, name = "vanilla_events", priority = 999)
public final class VanillaEventRecipeEditorTypes implements IModModule, RecipeImportHandler {
    public static final ResourceLocation ANVIL = ViScriptRecipe.id("anvil");
    public static final ResourceLocation BREWING = ViScriptRecipe.id("brewing");

    @Override
    public void registerRuntime(IEventBus modEventBus) {VanillaEventRecipeRuntime.register(modEventBus);}

    @Override
    public void registerEditorTypes() {
        register("anvil", ANVIL);
        register("brewing_stand", BREWING);
    }

    private void register(String workstation, ResourceLocation type) {
        var category = ResourceLocation.withDefaultNamespace(workstation);
        registerCategory(new RecipeEditorCategory(category, "viscript_recipe.editor.category.minecraft." + workstation, "minecraft", type));
        registerEditorType(RecipeEditorType.of(type, category, "viscript_recipe.editor.type.minecraft." + type.getPath(),
                VanillaEventRecipeData.class, VanillaEventRecipeCanvas.class));
    }

    @Override
    public RecipeImportHandler importHandler() {return this;}

    @Override
    public boolean canImport(RecipeHolder<?> holder) {return holder.value() instanceof VanillaEventRecipe;}

    @Override
    public RecipeImportResult tryImport(RecipeHolder<?> holder, HolderLookup.Provider provider) {
        if (!(holder.value() instanceof VanillaEventRecipe recipe)) return null;
        return RecipeImporter.success(RecipeImporter.baseEntry(holder.id(), recipe.editorType()).setData(recipe.data()));
    }
}
