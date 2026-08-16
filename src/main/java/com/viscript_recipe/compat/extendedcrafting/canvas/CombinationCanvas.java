package com.viscript_recipe.compat.extendedcrafting.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.extendedcrafting.data.ExtendedCraftingCombinationRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.views.NavigationView;

public class CombinationCanvas extends RecipeCanvas<ExtendedCraftingCombinationRecipeData> {
    public CombinationCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getInput());
        loadIngredients(data.getPedestalItems(), 1);
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setInput(getVisualIngredient(0)).setResult(getVisualOutput(0).getItem());
        data.setPedestalItems(getIngredients(8, 1));
    }

    @Override
    public UIElement createCanvas() {
        var slots = new IngredientDisplaySlot[9];
        for (int i = 0; i < slots.length; i++) slots[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(slots);
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(output);
        return ExtendedCraftingCanvasFactory.createCombinationCanvas(slots, output);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.extendedcrafting.combination"),
                intField("viscript_recipe.config.extendedcrafting.power_cost",
                        data.getPowerCost(), 0, Integer.MAX_VALUE, data::setPowerCost),
                intField("viscript_recipe.config.extendedcrafting.power_rate",
                        data.getPowerRate(), 0, Integer.MAX_VALUE, data::setPowerRate));
    }
}
