package com.viscript_recipe.compat.kaleidoscope_tavern.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.kaleidoscope_tavern.data.KaleidoscopeShakerRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.views.NavigationView;

import java.util.ArrayList;

public class ShakerCanvas extends RecipeCanvas<KaleidoscopeShakerRecipeData> {
    public ShakerCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredients(data.getIngredients());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredients(new ArrayList<>(getIngredients(3)));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var slots = new IngredientDisplaySlot[3];
        for (int i = 0; i < slots.length; i++) slots[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
        var result = createOutputSlot(0, JEI_SLOT_SIZE);
        return KaleidoscopeTavernCanvasFactory.createShakerCanvas(slots, result);
    }
}
