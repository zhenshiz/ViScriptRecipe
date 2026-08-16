package com.viscript_recipe.compat.mysticalagriculture.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.mysticalagriculture.data.MysticalAgricultureReprocessorRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;

import static com.viscript_recipe.compat.mysticalagriculture.canvas.AwakeningCanvas.useJeiCanvas;

public class ReprocessorCanvas extends RecipeCanvas<MysticalAgricultureReprocessorRecipeData> {
    public ReprocessorCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getInput());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        getData().setInput(getVisualIngredient(0)).setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var input = createIngredientSlot(0, 18);
        tooltip(input, "viscript_recipe.editor.mysticalagriculture.reprocessor.input");
        var output = createOutputSlot(0, 18);
        if (useJeiCanvas) {
            configureJeiOverlaySlotVisual(input);
            configureJeiOverlaySlotVisual(output);
        }
        return MysticalAgricultureCanvasFactory.createProcessCanvas(input, output, false, useJeiCanvas);
    }
}
