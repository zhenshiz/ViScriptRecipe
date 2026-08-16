package com.viscript_recipe.compat.ars_nouveau.canvas;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.ars_nouveau.data.ArsNouveauEnchantmentRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.editor.RecipeSearchComponents;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import static com.viscript_recipe.compat.ars_nouveau.canvas.ApparatusCanvas.*;

public class EnchantmentCanvas extends RecipeCanvas<ArsNouveauEnchantmentRecipeData> {
    public EnchantmentCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        var center = data.getLevel() <= 1 ? Items.BOOK : Items.ENCHANTED_BOOK;
        loadIngredientSlot(0, com.viscript_recipe.data.RecipeIngredient.item(center));
        loadPedestalInputs(this, data.getPedestalItems(), 8);
        updateSourceLabel(data.getSourceCost());
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
        tierLabel.setDisplay(false);
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
        content.addChild(RecipeSearchComponents.enchantment(
                "viscript_recipe.config.ars_nouveau.enchantment.enchantment",
                data::getEnchantment, data::setEnchantment, Runnables.doNothing()
        ));
        content.addChild(RecipeEditorUi.fieldGroup("viscript_recipe.config.ars_nouveau.enchantment.level",
                RecipeEditorUi.intField(data.getLevel(), 1, Integer.MAX_VALUE, data::setLevel)));
    }
}
