package com.viscript_recipe.compat.kaleidoscope_cookery.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.kaleidoscope_cookery.data.KaleidoscopeMillstoneRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;

public class MillstoneCanvas extends RecipeCanvas<KaleidoscopeMillstoneRecipeData> {
    public MillstoneCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getIngredient());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        getData().setIngredient(getVisualIngredient(0)).setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        return KaleidoscopeCanvasFactory.createMillstoneCanvas(
                createIngredientSlot(0, JEI_SLOT_SIZE), createOutputSlot(0, JEI_SLOT_SIZE));
    }
}
