package com.viscript_recipe.compat.extendedcrafting.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.extendedcrafting.data.ExtendedCraftingFluxCrafterRecipeData;
import com.viscript_recipe.compat.extendedcrafting.data.ExtendedCraftingRecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.canvas.ShapedGridHelper;
import com.viscript_recipe.gui.views.NavigationView;

public class FluxCrafterCanvas extends RecipeCanvas<ExtendedCraftingFluxCrafterRecipeData> {
    public FluxCrafterCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        if (isShaped()) ShapedGridHelper.loadGrid(this, data.getPattern(), data.getKey(), 3, 3, 3);
        else loadIngredients(data.getShapelessIngredients());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        if (isShaped()) {
            var pattern = ShapedGridHelper.saveGrid(this, 3, 3, 3);
            data.setPattern(pattern.pattern()).setKey(pattern.key());
        } else data.setShapelessIngredients(getIngredients(9));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var slots = new UIElement[9];
        for (int i = 0; i < slots.length; i++) slots[i] = createIngredientSlot(i, SLOT_SIZE);
        return ExtendedCraftingCanvasFactory.createFluxCanvas(slots, createOutputSlot(0, OUTPUT_SLOT_SIZE));
    }

    private boolean isShaped() { return ExtendedCraftingRecipeEditorTypes.isShapedFluxType(entry.getType()); }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.extendedcrafting.flux_crafter"),
                intField("viscript_recipe.config.extendedcrafting.flux_crafter.power_required",
                        data.getPowerRequired(), 0, Integer.MAX_VALUE, data::setPowerRequired),
                intField("viscript_recipe.config.extendedcrafting.power_rate",
                        data.getPowerRate(), 0, Integer.MAX_VALUE, data::setPowerRate));
    }
}
