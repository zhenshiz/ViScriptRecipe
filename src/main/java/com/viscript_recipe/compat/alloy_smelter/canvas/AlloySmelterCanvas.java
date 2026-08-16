package com.viscript_recipe.compat.alloy_smelter.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.alloy_smelter.data.AlloySmelterRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.IngredientDisplaySlot;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class AlloySmelterCanvas extends RecipeCanvas<AlloySmelterRecipeData> {
    static final Label timeLabel = RecipeEditorUi.label(Component.empty());
    static final Label fuelLabel = RecipeEditorUi.label(Component.empty());
    static final Label tierLabel = RecipeEditorUi.label(Component.empty());
    static final UIElement fuelHint = createItemIcon(Items.COAL.getDefaultInstance(), JEI_SLOT_SIZE)
            .style(style -> style.tooltips(Component.translatable("viscript_recipe.editor.alloy_smelter.fuel_hint")));

    public AlloySmelterCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public boolean ingredientHasCount(int slotIndex) {return true;}

    @Override
    public void load() {
        var data = getData();
        loadIngredients(data.getMaterials());
        setVisualOutput(0, data.getResult());
        updateLabels();
    }

    @Override
    public void save() {
        var data = getData();
        data.setMaterials(getIngredients(AlloySmelterRecipeData.MAX_INPUTS));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var inputs = new IngredientDisplaySlot[AlloySmelterRecipeData.MAX_INPUTS];
        for (int i = 0; i < inputs.length; i++) {
            inputs[i] = createIngredientSlot(i, JEI_SLOT_SIZE);
            configureJeiOverlaySlotVisual(inputs[i]);
        }
        var output = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(output);
        return AlloySmelterCanvasFactory.create(inputs, output, fuelHint, timeLabel, fuelLabel, tierLabel, RecipeCanvas::selectRecipe);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(RecipeEditorUi.sectionTitle("viscript_recipe.editor.category.alloy_smelter.smelting"),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.alloy_smelter.smelting_time",
                        RecipeEditorUi.intField(data.getSmeltingTime(), 1, Integer.MAX_VALUE, value -> {
                            data.setSmeltingTime(value); updateLabels();
                        })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.alloy_smelter.fuel_per_tick",
                        RecipeEditorUi.intField(data.getFuelPerTick(), 0, Integer.MAX_VALUE, value -> {
                            data.setFuelPerTick(value); updateLabels();
                        })),
                RecipeEditorUi.fieldGroup("viscript_recipe.config.alloy_smelter.required_tier",
                        RecipeEditorUi.intField(data.getRequiredTier(), 1, Integer.MAX_VALUE, value -> {
                            data.setRequiredTier(value); updateLabels();
                        })));
    }

    private void updateLabels() {
        var data = getData();
        timeLabel.setText(Component.translatable("viscript_recipe.editor.alloy_smelter.smelting_time", Math.max(1, data.getSmeltingTime())));
        fuelLabel.setText(Component.translatable("viscript_recipe.editor.alloy_smelter.fuel_per_tick", Math.max(0, data.getFuelPerTick())));
        tierLabel.setText(Component.translatable("viscript_recipe.editor.alloy_smelter.tier", Math.max(1, data.getRequiredTier())));
    }
}
