package com.viscript_recipe.compat.kaleidoscope_cookery.canvas;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.kaleidoscope_cookery.data.KaleidoscopePotRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;

public class PotCanvas extends RecipeCanvas<KaleidoscopePotRecipeData> {
    static final Label stirFryLabel = RecipeEditorUi.label(Component.empty());
    static {configureLabel(stirFryLabel);}

    public PotCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredients(data.getIngredients());
        loadIngredientSlot(9, data.getCarrier());
        setVisualOutput(0, data.getResult());
        updateStirFryLabel(data.getStirFryCount());
    }

    @Override
    public void save() {
        var data = getData();
        data.setIngredients(getIngredients(9));
        data.setCarrier(getVisualIngredient(9));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var slots = new IngredientDisplaySlot[9];
        for (int i = 0; i < slots.length; i++) slots[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(slots);
        var carrier = createIngredientSlot(9, JEI_SLOT_SIZE);
        var result = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(carrier, result);
        return KaleidoscopeCanvasFactory.createPotCanvas(slots, carrier, result, stirFryLabel);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.kaleidoscope_cookery"),
                intField("viscript_recipe.config.kaleidoscope_cookery.time",
                        data.getTime(), 1, Integer.MAX_VALUE, data::setTime),
                intField("viscript_recipe.config.kaleidoscope_cookery.stir_fry_count",
                        data.getStirFryCount(), 0, Integer.MAX_VALUE, value -> {
                            data.setStirFryCount(value); updateStirFryLabel(value);
                        })
        );
    }

    private void updateStirFryLabel(int count) {
        stirFryLabel.setText(Component.translatable("jei.kaleidoscope_cookery.pot.stir_fry_count", count));
    }

    static void configureLabel(Label label) {
        label.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER)
                .textColor(ColorPattern.GRAY.color).textWrap(TextWrap.HOVER_ROLL));
    }
}
