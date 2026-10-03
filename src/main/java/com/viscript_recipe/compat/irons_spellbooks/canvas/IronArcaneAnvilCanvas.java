package com.viscript_recipe.compat.irons_spellbooks.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.irons_spellbooks.data.IronArcaneAnvilRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;

public class IronArcaneAnvilCanvas extends RecipeCanvas<IronArcaneAnvilRecipeData> {
    static final boolean useJeiCanvas = ArcaneAnvilCanvasFactory.hasJeiSkin();

    public IronArcaneAnvilCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var arcaneAnvil = getData();
        loadIngredientSlot(0, arcaneAnvil.getInput());
        loadIngredientSlot(1, arcaneAnvil.getMaterial());
        setVisualOutput(0, arcaneAnvil.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setInput(getVisualIngredient(0));
        data.setMaterial(getVisualIngredient(1));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        // 怎么还有没有jei就摆烂的canvas？
        if (!useJeiCanvas) return new UIElement().setDisplay(false);
        UIElement[] inputSlots = new UIElement[2];
        for (int index = 0; index < 2; index++) {
            var slot = createIngredientSlot(index, JEI_SLOT_SIZE);
            configureJeiOverlaySlotVisual(slot);
            inputSlots[index] = slot;
        }
        var outputSlot = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(outputSlot);
        return ArcaneAnvilCanvasFactory.createCanvas(inputSlots[0], inputSlots[1], outputSlot);
    }
}
