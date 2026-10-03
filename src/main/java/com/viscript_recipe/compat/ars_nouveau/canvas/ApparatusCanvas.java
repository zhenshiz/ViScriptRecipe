package com.viscript_recipe.compat.ars_nouveau.canvas;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.ars_nouveau.data.ArsNouveauApparatusRecipeData;
import com.viscript_recipe.compat.ars_nouveau.data.IPreview;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ApparatusCanvas extends RecipeCanvas<ArsNouveauApparatusRecipeData> {
    public static final int pedestalCount = 8;
    static final Label sourceLabel = emptyLabel();
    static final Label tierLabel = emptyLabel();

    public ApparatusCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getReagent());
        loadIngredients(data.getPedestalItems(), 1);
        setVisualOutput(0, data.getResult());
        updateSourceLabel(data.getSourceCost());
    }

    @Override
    public void save() {
        var data = getData();
        data.setReagent(getVisualIngredient(0));
        data.setPedestalItems(getIngredients(pedestalCount, 1, true));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var slots = new UIElement[9];
        var cells = new UIElement[9];
        for (int i = 0; i < slots.length; i++) slots[i] = createIngredientSlot(i, SLOT_SIZE);
        var result = createOutputSlot(0, OUTPUT_SLOT_SIZE);
        tierLabel.setDisplay(false);
        return ArsNouveauCanvasFactory.createApparatusCanvas(slots, cells, result,
                centerPreview(getData()), outputPreview(getData()), sourceLabel, tierLabel, this::getSlotTooltip);
    }

    private Component getSlotTooltip(int index) {
        return Component.translatable("viscript_recipe.editor.ars_nouveau.ingredient_slot",
                Component.translatable(index == 0 ?  "viscript_recipe.editor.ars_nouveau.reagent" :
                        "viscript_recipe.editor.ars_nouveau.pedestal_item"));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChild(sectionTitle("viscript_recipe.editor.properties.ars_nouveau"));
        addSourceField(content, data.getSourceCost(), data::setSourceCost);
        content.addChild(switchField("viscript_recipe.config.ars_nouveau.apparatus.keep_nbt_of_reagent",
                data.isKeepNbtOfReagent(), data::setKeepNbtOfReagent));
    }

    static void addSourceField(UIElement content, int sourceCost, Consumer<Integer> setSourceCost) {
        content.addChild(intField("viscript_recipe.config.ars_nouveau.source_cost",
                sourceCost, 0, Integer.MAX_VALUE, value -> {
                    setSourceCost.accept(value); updateSourceLabel(value);
                }));
    }

    static UIElement centerPreview(IPreview preview) {
        var centerPreview = createItemIcon(preview.centerPreview(), SLOT_SIZE);
        tooltip(centerPreview, "viscript_recipe.editor.ars_nouveau.center_preview");
        return centerPreview;
    }

    static UIElement outputPreview(IPreview preview) {
        var outputPreview = createItemIcon(preview.outputPreview(), OUTPUT_SLOT_SIZE);
        tooltip(outputPreview, "viscript_recipe.editor.ars_nouveau.output_preview");
        return outputPreview;
    }

    static void updateSourceLabel(int source) {
        sourceLabel.setText(source <= 0 ? Component.empty() : Component.translatable("ars_nouveau.source", source));
    }

     public static Label emptyLabel() {
        var label = RecipeCanvas.emptyLabel();
        label.textStyle(style -> style.textColor(ColorPattern.WHITE.color).textWrap(TextWrap.HOVER_ROLL));
        return label;
    }
}
