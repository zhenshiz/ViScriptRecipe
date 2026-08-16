package com.viscript_recipe.compat.mysticalagriculture.canvas;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeUiSupport;
import com.viscript_recipe.compat.mysticalagriculture.data.MysticalAgricultureSoulExtractionRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;

import static com.viscript_recipe.compat.mysticalagriculture.canvas.AwakeningCanvas.useJeiCanvas;

public class SoulExtractionCanvas extends RecipeCanvas<MysticalAgricultureSoulExtractionRecipeData> {
    static final IngredientDisplaySlot preview =
            EnchanterCanvas.readOnlySlot("viscript_recipe.editor.mysticalagriculture.soul_extraction.result");

    public SoulExtractionCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        loadIngredientSlot(0, getData().getInput());
        setResultPreview();
    }

    @Override
    public void save() {
        getData().setInput(getVisualIngredient(0));
    }

    @Override
    public UIElement createCanvas() {
        var input = createIngredientSlot(0, MysticalAgricultureCanvasFactory.SLOT_SIZE);
        tooltip(input, "viscript_recipe.editor.mysticalagriculture.soul_extraction.input");
        if (useJeiCanvas) {
            configureJeiOverlaySlotVisual(input);
            configureJeiOverlaySlotVisual(preview);
        }
        return MysticalAgricultureCanvasFactory.createProcessCanvas(input, preview, false, useJeiCanvas);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.mysticalagriculture.soul_extraction"),
                MysticalAgricultureSearchComponents.mobSoulType(
                        "viscript_recipe.config.mysticalagriculture.soul_extraction.soul_type",
                        data::getSoulType, id -> {
                            data.setSoulType(id); setResultPreview();
                        }, Runnables.doNothing()),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.mysticalagriculture.soul_extraction.souls",
                        RecipeEditorUi.doubleField(data.getSouls(), 0, Double.MAX_VALUE, value -> {
                             data.setSouls(value); setResultPreview();
                        }))
        );
    }

    private void setResultPreview() {
        preview.setItem(MysticalAgricultureRecipeUiSupport.soulJar(getData()), false);
    }
}
