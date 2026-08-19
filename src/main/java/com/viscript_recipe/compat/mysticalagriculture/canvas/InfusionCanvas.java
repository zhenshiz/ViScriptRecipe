package com.viscript_recipe.compat.mysticalagriculture.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.mysticalagriculture.data.MysticalAgricultureInfusionRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;

import static com.viscript_recipe.compat.mysticalagriculture.canvas.AwakeningCanvas.useJeiCanvas;

public class InfusionCanvas extends RecipeCanvas<MysticalAgricultureInfusionRecipeData> {
    public InfusionCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getInput());
        loadIngredients(data.getIngredients(), 1);
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setInput(getVisualIngredient(0)).setResult(getVisualOutput(0).getItem());
        data.setIngredients(getIngredients(MysticalAgricultureInfusionRecipeData.MAX_PEDESTAL_INGREDIENTS, 1));
    }

    @Override
    public UIElement createCanvas() {
        var input = createIngredientSlot(0, 18);
        tooltip(input, "viscript_recipe.editor.mysticalagriculture.infusion.input");
        var output = createOutputSlot(0, 18);
        if (useJeiCanvas) {
            configureJeiOverlaySlotVisual(input);
            configureJeiOverlaySlotVisual(output);
        }
        var pedestals = new UIElement[8];
        for (int index = 0; index < 8; index++) {
            var slot = createIngredientSlot(index + 1, 18);
            tooltip(slot, Component.translatable("viscript_recipe.editor.mysticalagriculture.infusion.pedestal", index + 1));
            if (useJeiCanvas) configureJeiOverlaySlotVisual(slot);
            pedestals[index] = slot;
        }
        return MysticalAgricultureCanvasFactory.createInfusionCanvas(input, pedestals, output, useJeiCanvas);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.mysticalagriculture.infusion"),
                switchField("viscript_recipe.config.mysticalagriculture.transfer_components",
                        data.isTransferComponents(), data::setTransferComponents));
    }
}
