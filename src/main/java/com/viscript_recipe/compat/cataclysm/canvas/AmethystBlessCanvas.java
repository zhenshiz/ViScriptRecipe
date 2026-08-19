package com.viscript_recipe.compat.cataclysm.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.cataclysm.data.CataclysmAmethystBlessRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;

public class AmethystBlessCanvas extends RecipeCanvas<CataclysmAmethystBlessRecipeData> {
    public AmethystBlessCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getIngredient());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredient(getVisualIngredient(0));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var ingredient = createIngredientSlot(0, JEI_SLOT_SIZE);
        var result = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(ingredient, result);
        tooltip(ingredient, "viscript_recipe.editor.cataclysm.amethyst_bless.ingredient_slot");
        return CataclysmCanvasFactory.createAmethystBlessCanvas(ingredient, result);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.cataclysm.amethyst_bless"),
                intField("viscript_recipe.config.cataclysm.amethyst_bless.time",
                        data.getTime(), 1, Integer.MAX_VALUE, data::setTime));
    }
}
