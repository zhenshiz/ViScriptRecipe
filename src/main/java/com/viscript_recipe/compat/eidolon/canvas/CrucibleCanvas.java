package com.viscript_recipe.compat.eidolon.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.eidolon.data.EidolonCrucibleRecipeData;
import com.viscript_recipe.compat.eidolon.data.EidolonCrucibleStepData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;

import java.util.Collections;

public final class CrucibleCanvas extends EidolonItemCanvas<EidolonCrucibleRecipeData> {
    private int stepIndex;
    public CrucibleCanvas(NavigationView navigation, RecipeEntry entry) { super(navigation, entry); }

    @Override
    public void load() { super.load(); setVisualOutput(0, getData().getResult()); }

    @Override
    public void save() { super.save(); getData().setResult(getVisualOutput(0).getItem()); }

    @Override
    public UIElement createCanvas() {
        if (getData().getSteps().isEmpty()) getData().getSteps().add(new EidolonCrucibleStepData());
        stepIndex = Math.min(stepIndex, getData().getSteps().size() - 1);
        var step = getData().getSteps().get(stepIndex);
        int slots = step.getItems().size();
        int width = 164;
        int rows = (slots + 5) / 6;
        int height = Math.max(172, 92 + rows * 20);
        var panel = EidolonCanvasFactory.page("jei_page_bg.png", width, height);
        panel.addChild(EidolonCanvasFactory.label(text("step", stepIndex + 1, getData().getSteps().size()), 5, 12, width - 10));
        panel.addChild(EidolonCanvasFactory.label(text("stirs", step.getStirs()), 5, 31, width - 10));
        for (int i = 0; i < slots; i++) panel.addChild(EidolonCanvasFactory.slot(
                inputSlot(i, step.getItems().get(i), text("crucible_input", stepIndex + 1, i + 1)), 16 + i % 6 * 22, 55 + i / 6 * 20));
        var output = createOutputSlot(0, JEI_SLOT_SIZE); output.setId("eidolon_output");
        panel.addChild(EidolonCanvasFactory.slot(output, 73, height - 35));
        return EidolonCanvasFactory.centered(panel);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var steps = getData().getSteps();
        var step = steps.get(stepIndex);
        content.addChildren(sectionTitle("viscript_recipe.editor.type.eidolon.crucible"),
                intField("viscript_recipe.config.eidolon.step", stepIndex + 1, 1, steps.size(), value -> showStep(value - 1)),
                intField("viscript_recipe.config.eidolon.steps", steps.size(), 1, Math.max(64, steps.size()), value -> {
                    save();
                    while (steps.size() < value) steps.add(new EidolonCrucibleStepData());
                    while (steps.size() > value) steps.removeLast();
                    rebuild();
                }),
                intField("viscript_recipe.config.eidolon.stirs", step.getStirs(), 0, Integer.MAX_VALUE,
                        value -> { save(); step.setStirs(value); rebuild(); }),
                intField("viscript_recipe.config.eidolon.step_slots", step.getItems().size(), 0, MAX_INGREDIENT,
                        value -> { save(); resize(step.getItems(), value); rebuild(); }),
                RecipeEditorUi.textButton(text("move_step_up"), null, event -> moveStep(-1)),
                RecipeEditorUi.textButton(text("move_step_down"), null, event -> moveStep(1)));
    }

    private void moveStep(int direction) {
        int next = stepIndex + direction;
        if (next < 0 || next >= getData().getSteps().size()) return;
        save(); Collections.swap(getData().getSteps(), stepIndex, next); stepIndex = next; rebuild();
    }

    public void showStep(int index) {
        save(); stepIndex = Math.clamp(index, 0, getData().getSteps().size() - 1); rebuild();
    }
}
