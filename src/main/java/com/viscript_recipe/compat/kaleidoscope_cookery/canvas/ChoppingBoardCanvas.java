package com.viscript_recipe.compat.kaleidoscope_cookery.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.kaleidoscope_cookery.data.KaleidoscopeChoppingBoardRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;

public class ChoppingBoardCanvas extends RecipeCanvas<KaleidoscopeChoppingBoardRecipeData> {
    public ChoppingBoardCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

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
        return KaleidoscopeCanvasFactory.createChoppingBoardCanvas(
                createIngredientSlot(0, JEI_SLOT_SIZE), createOutputSlot(0, JEI_SLOT_SIZE));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.kaleidoscope_cookery"),
                intField("viscript_recipe.config.kaleidoscope_cookery.cut_count",
                        data.getCutCount(), 1, Integer.MAX_VALUE, data::setCutCount),
                resourceField("viscript_recipe.config.kaleidoscope_cookery.model_id",
                        data.getModelId(), data::setModelId)
        );
    }
}
