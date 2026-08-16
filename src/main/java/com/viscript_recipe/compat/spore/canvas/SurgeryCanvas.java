package com.viscript_recipe.compat.spore.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.spore.data.SporeSurgeryRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.views.NavigationView;

public class SurgeryCanvas extends RecipeCanvas<SporeSurgeryRecipeData> {
    public SurgeryCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredients(data.getIngredients());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredients(getIngredients(SporeSurgeryRecipeData.INPUT_COUNT));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var inputs = new IngredientDisplaySlot[SporeSurgeryRecipeData.INPUT_COUNT];
        for (int i = 0; i < inputs.length; i++) inputs[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(inputs);
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(output);
        return SporeCanvasFactory.createSurgeryCanvas(inputs, output);
    }
}
