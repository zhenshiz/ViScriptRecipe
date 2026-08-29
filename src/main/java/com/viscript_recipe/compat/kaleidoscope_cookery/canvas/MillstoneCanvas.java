package com.viscript_recipe.compat.kaleidoscope_cookery.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.viscript_recipe.compat.kaleidoscope_cookery.data.KaleidoscopeMillstoneRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.data.RecipeOutputData;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;

import java.util.ArrayList;

public class MillstoneCanvas extends RecipeCanvas<KaleidoscopeMillstoneRecipeData> {
    public MillstoneCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getIngredient());
        var results = data.getResolvedResults();
        for (int i = 0; i < Math.min(KaleidoscopeMillstoneRecipeData.MAX_RESULTS, results.size()); i++) {
            setVisualOutput(i, results.get(i));
        }
    }

    @Override
    public void save() {
        var results = new ArrayList<RecipeOutputData>();
        for (int i = 0; i < KaleidoscopeMillstoneRecipeData.MAX_RESULTS; i++) {
            var output = getVisualOutput(i);
            if (!output.isEmpty()) {
                results.add(output);
            }
        }
        getData().setIngredient(getVisualIngredient(0)).setResults(results);
    }

    @Override
    public void buildResultProperties(UIElement content) {
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.kaleidoscope_cookery.millstone_result"),
                PropertiesView.createItemStackConfigurator(
                        "viscript_recipe.config.kaleidoscope_cookery.millstone.result_item",
                        () -> getSelectedOutput().getItem(), this::setSelectedOutput),
                floatField("viscript_recipe.config.kaleidoscope_cookery.millstone.chance",
                        getSelectedOutput().getChance(), 0.0F, 1.0F, this::setSelectedOutput));
    }

    @Override
    public UIElement createCanvas() {
        var input = createIngredientSlot(0, JEI_SLOT_SIZE);
        var results = new ItemSlot[KaleidoscopeMillstoneRecipeData.MAX_RESULTS];
        for (int i = 0; i < results.length; i++) {
            results[i] = createOutputSlot(i, JEI_SLOT_SIZE);
        }
        configureJeiOverlaySlotVisual(input);
        configureJeiOverlaySlotVisual(results);
        return KaleidoscopeCanvasFactory.createMillstoneCanvas(input, results);
    }
}
