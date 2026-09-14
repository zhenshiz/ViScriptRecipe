package com.viscript_recipe.compat.extendedcrafting.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.extendedcrafting.data.ExtendedCraftingEnderCrafterRecipeData;
import com.viscript_recipe.compat.extendedcrafting.data.ExtendedCraftingRecipeEditorTypes;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.canvas.ShapedGridHelper;
import com.viscript_recipe.gui.canvas.vanilla.BasicRecipeCanvasFactory;
import com.viscript_recipe.gui.editor.RecipeGridFactory;
import com.viscript_recipe.gui.views.NavigationView;

public class EnderCrafterCanvas extends RecipeCanvas<ExtendedCraftingEnderCrafterRecipeData> {
    public EnderCrafterCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

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
        var grid = RecipeGridFactory.borderedGrid(3, 3, SLOT_SIZE,
                (index, row, col) -> createIngredientSlot(index, SLOT_SIZE));
        return BasicRecipeCanvasFactory.createCraftingCanvas(grid, createOutputSlot(0, OUTPUT_SLOT_SIZE));
    }

    private boolean isShaped() { return ExtendedCraftingRecipeEditorTypes.isShapedEnderType(entry.getType()); }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.extendedcrafting.ender_crafter"),
                intField("viscript_recipe.config.extendedcrafting.ender_crafter.crafting_time",
                        data.getCraftingTime(), 0, Integer.MAX_VALUE, data::setCraftingTime));
    }
}
