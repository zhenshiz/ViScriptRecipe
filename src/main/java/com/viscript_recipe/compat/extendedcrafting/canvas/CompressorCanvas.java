package com.viscript_recipe.compat.extendedcrafting.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.extendedcrafting.data.ExtendedCraftingCompressorRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;

public class CompressorCanvas extends RecipeCanvas<ExtendedCraftingCompressorRecipeData> {
    public CompressorCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public boolean ingredientHasCount(int slotIndex) {return slotIndex > 0;}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getCatalyst());
        loadIngredients(data.getInputs(), 1);
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setCatalyst(getVisualIngredient(0));
        data.setInputs(getIngredients(1, 1, true));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var catalyst = createIngredientSlot(0, JEI_SLOT_SIZE);
        var input = createIngredientSlot(1, JEI_SLOT_SIZE);
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(catalyst, input, output);
        return ExtendedCraftingCanvasFactory.createCompressorCanvas(catalyst, input, output);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.extendedcrafting.compressor"),
                intField("viscript_recipe.config.extendedcrafting.power_cost",
                        data.getPowerCost(), 0, Integer.MAX_VALUE, data::setPowerCost),
                intField("viscript_recipe.config.extendedcrafting.power_rate",
                        data.getPowerRate(), 0, Integer.MAX_VALUE, data::setPowerRate));
    }
}
