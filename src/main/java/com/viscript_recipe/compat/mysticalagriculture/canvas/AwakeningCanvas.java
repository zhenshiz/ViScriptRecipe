package com.viscript_recipe.compat.mysticalagriculture.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.mysticalagriculture.data.MysticalAgricultureAwakeningRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import net.minecraft.network.chat.Component;

public class AwakeningCanvas extends RecipeCanvas<MysticalAgricultureAwakeningRecipeData> {
    static final boolean useJeiCanvas = MysticalAgricultureCanvasFactory.hasJeiSkin();

    public AwakeningCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getInput());
        loadIngredients(data.getIngredients(), 1);
        for (int index = 0; index < 4; index++) setVisualOutput(index + 1, data.essence(index));
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setInput(getVisualIngredient(0)).setResult(getVisualOutput(0).getItem());
        data.setIngredients(getIngredients(4, 1, true));
        for (int index = 0; index < 4; index++) data.setEssence(index, getVisualOutput(index + 1).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var input = createIngredientSlot(0, MysticalAgricultureCanvasFactory.SLOT_SIZE);
        tooltip(input, "viscript_recipe.editor.mysticalagriculture.awakening.input");
        var output = createOutputSlot(0, MysticalAgricultureCanvasFactory.SLOT_SIZE);
        if (useJeiCanvas) configureJeiOverlaySlotVisual(input, output);
        var ingredients = new UIElement[4];
        var essences = new UIElement[4];
        for (int index = 0; index < ingredients.length; index++) {
            var slot = createIngredientSlot(index + 1, MysticalAgricultureCanvasFactory.SLOT_SIZE);
            tooltip(slot, Component.translatable("viscript_recipe.editor.mysticalagriculture.awakening.ingredient", index + 1));
            var essenceSlot = createOutputSlot(index + 1, 18);
            tooltip(essenceSlot, Component.translatable("viscript_recipe.editor.mysticalagriculture.awakening.essence", index + 1));
            if (useJeiCanvas) configureJeiOverlaySlotVisual(slot, essenceSlot);
            ingredients[index] = slot;
            essences[index] = essenceSlot;
        }
        return MysticalAgricultureCanvasFactory.createAwakeningCanvas(input, ingredients, essences, output, useJeiCanvas);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.mysticalagriculture.awakening"),
                switchField("viscript_recipe.config.mysticalagriculture.transfer_components",
                        getData().isTransferComponents(), getData()::setTransferComponents));
    }

    @Override
    public void buildResultProperties(UIElement content) {
        if (selectedSlotIndex() == 0) super.buildResultProperties(content);
        else {
            content.addChildren(sectionTitle("viscript_recipe.config.mysticalagriculture.awakening.essences"),
                    PropertiesView.createItemStackConfigurator(
                            "viscript_recipe.config.mysticalagriculture.awakening.essences",
                            () -> getSelectedOutput().getItem(), this::setSelectedOutput
                    )
            );
        }
    }
}
