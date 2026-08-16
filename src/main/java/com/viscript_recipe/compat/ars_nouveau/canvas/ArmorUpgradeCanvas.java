package com.viscript_recipe.compat.ars_nouveau.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.ars_nouveau.data.ArsNouveauArmorUpgradeRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;

import static com.viscript_recipe.compat.ars_nouveau.canvas.ApparatusCanvas.*;

public class ArmorUpgradeCanvas extends RecipeCanvas<ArsNouveauArmorUpgradeRecipeData> {
    public ArmorUpgradeCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadPedestalInputs(this, data.getPedestalItems(), 8);
        updateSourceLabel(data.getSourceCost());
        updateTierLabel(data.getTier() + 1);
    }

    @Override
    public void save() {
        getData().setPedestalItems(savePedestalInputs(this, 8));
    }

    @Override
    public UIElement createCanvas() {
        var slots = new UIElement[9];
        var cells = new UIElement[9];
        for (int i = 0; i < slots.length; i++) slots[i] = createIngredientSlot(i, SLOT_SIZE);
        slots[0].setDisplay(false);
        var result = createOutputSlot(0, OUTPUT_SLOT_SIZE);
        result.setDisplay(false);
        var centerPreview = createItemIcon(getData().centerPreview(), SLOT_SIZE).style(style ->
                style.tooltips(Component.translatable("viscript_recipe.editor.ars_nouveau.center_preview")));
        var outputPreview = createItemIcon(getData().outputPreview(), OUTPUT_SLOT_SIZE).style(style ->
                style.tooltips(Component.translatable("viscript_recipe.editor.ars_nouveau.output_preview")));
        tierLabel.setDisplay(true);
        return ArsNouveauCanvasFactory.createApparatusCanvas(slots, cells, result, centerPreview, outputPreview,
                sourceLabel, tierLabel, this::getSlotTooltip);
    }

    private Component getSlotTooltip(int index) {
        return Component.translatable("viscript_recipe.editor.ars_nouveau.ingredient_slot",
                Component.translatable("viscript_recipe.editor.ars_nouveau.pedestal_item"));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChild(RecipeEditorUi.sectionTitle("viscript_recipe.editor.properties.ars_nouveau"));
        addSourceField(content, data.getSourceCost(), data::setSourceCost);
        content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.armor_upgrade.tier",
                RecipeEditorUi.intField(data.getTier() + 1, 2, Integer.MAX_VALUE,
                        value -> { data.setTier(value - 1); updateTierLabel(value); })));
    }

    static void updateTierLabel(int tier) {
        tierLabel.setText(Component.translatable("ars_nouveau.tier", tier));
    }
}
