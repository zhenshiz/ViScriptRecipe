package com.viscript_recipe.compat.industrial_foregoing.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.industrial_foregoing.data.IndustrialStoneWorkRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;

public class StoneWorkCanvas extends RecipeCanvas<IndustrialStoneWorkRecipeData> {
    static final Label needsLabel = RecipeEditorUi.label(Component.empty());
    static final Label consumesLabel = RecipeEditorUi.label(Component.empty());

    public StoneWorkCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        setVisualOutput(0, getData().getOutput());
        updateLabels();
    }

    @Override
    public void save() {
        getData().setOutput(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(output);
        return IndustrialForegoingCanvasFactory.createStoneWork(
                IndustrialForegoingCanvasFactory.slotCell(output, 18, 18), needsLabel, consumesLabel);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.industrial_foregoing.stonework"),
                intField("viscript_recipe.config.industrial_foregoing.stonework.water_need",
                        data.getWaterNeed(), 0, Integer.MAX_VALUE, value -> {
                    data.setWaterNeed(value); updateLabels();
                }),
                intField("viscript_recipe.config.industrial_foregoing.stonework.lava_need",
                        data.getLavaNeed(), 0, Integer.MAX_VALUE, value -> {
                    data.setLavaNeed(value); updateLabels();
                }),
                intField("viscript_recipe.config.industrial_foregoing.stonework.water_consume",
                        data.getWaterConsume(), 0, Integer.MAX_VALUE, value -> {
                    data.setWaterConsume(value); updateLabels();
                }),
                intField("viscript_recipe.config.industrial_foregoing.stonework.lava_consume",
                        data.getLavaConsume(), 0, Integer.MAX_VALUE, value -> {
                    data.setLavaConsume(value); updateLabels();
                }));
    }

    private void updateLabels() {
        var data = getData();
        needsLabel.setText(Component.translatable("viscript_recipe.editor.industrial_foregoing.stonework.needs",
                data.getWaterNeed(), data.getLavaNeed()));
        consumesLabel.setText(Component.translatable("viscript_recipe.editor.industrial_foregoing.stonework.consumes",
                data.getWaterConsume(), data.getLavaConsume()));
    }
}
