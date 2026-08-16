package com.viscript_recipe.compat.extendedcrafting.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.extendedcrafting.data.ExtendedCraftingTableRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.canvas.ShapedGridHelper;
import com.viscript_recipe.gui.canvas.vanilla.BasicRecipeCanvasFactory;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeGridFactory;
import com.viscript_recipe.gui.views.NavigationView;

import java.util.List;

import static com.viscript_recipe.compat.extendedcrafting.data.ExtendedCraftingRecipeEditorTypes.*;

public class CraftingTableCanvas extends RecipeCanvas<ExtendedCraftingTableRecipeData> {
    public CraftingTableCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        int size = gridSize();
        if (isShaped()) {
            ShapedGridHelper.loadGrid(this, data.getPattern(), data.getKey(), size, size, 9);
        } else loadIngredients(data.getShapelessIngredients());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        int size = gridSize();
        if (isShaped()) {
            var pattern = ShapedGridHelper.saveGrid(this, size, size, 9);
            data.setPattern(pattern.pattern()).setKey(pattern.key());
        } else data.setShapelessIngredients(getIngredients(size * size));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        int size = gridSize();
        var grid = RecipeGridFactory.borderedGrid(size, size, SLOT_SIZE,
                (index, row, col) -> createIngredientSlot(row * 9 + col, SLOT_SIZE));
        return BasicRecipeCanvasFactory.createCraftingCanvas(grid, createOutputSlot(0, OUTPUT_SLOT_SIZE));
    }

    private boolean isShaped() {return isShapedTableType(entry.getType());}

    private int effectiveTier() {
        int tier = getData().getTier();
        return tier == 0 ? tableTierForType(entry.getType()) : tier;
    }

    private int gridSize() {return tableGridSizeForTier(effectiveTier());}

    @Override
    public void buildRecipeProperties(UIElement content) {
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.extendedcrafting.table"),
                field("viscript_recipe.config.extendedcrafting.table.size",
                        RecipeEditorUi.selector(List.of(1, 2, 3, 4), effectiveTier(),
                                tier -> {
                                    int size = tableGridSizeForTier(tier);
                                    return net.minecraft.network.chat.Component.translatable(
                                            "viscript_recipe.config.extendedcrafting.table.size.value", size, size);
                                }, tier -> {
                                    int size = tableGridSizeForTier(tier);
                                    getData().setTier(tier).setWidth(size).setHeight(size);
                                    reloadCanvas();
                                }))
        );
    }
}
