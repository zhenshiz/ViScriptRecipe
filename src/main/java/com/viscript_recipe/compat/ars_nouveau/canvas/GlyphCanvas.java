package com.viscript_recipe.compat.ars_nouveau.canvas;

import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.viscript_recipe.compat.ars_nouveau.data.ArsNouveauGlyphRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.recipe.RecipeHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

public class GlyphCanvas extends RecipeCanvas<ArsNouveauGlyphRecipeData> {
    static final UIElement workstationIcon = new UIElement();
    static final Label expLabel = emptyLabel();

    static {
        workstationIcon.style(style -> style
                .backgroundTexture(new ItemStackTexture(RecipeHelper.registryItem("ars_nouveau:scribes_table", Items.LECTERN)))
                .tooltips(Component.translatable("block.ars_nouveau.scribes_table")));
        expLabel.textStyle(style -> style.textAlignHorizontal(Horizontal.LEFT)
                .textColor(ColorPattern.BLACK.color).textWrap(TextWrap.HOVER_ROLL));
    }

    public GlyphCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredients(data.getInputs());
        setVisualOutput(0, data.getResult());
        updateExpLabel();
    }

    @Override
    public void save() {
        var data = getData();
        data.setInputs(getIngredients(9));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var slots = new UIElement[9];
        var cells = new UIElement[slots.length];
        for (int i = 0; i < slots.length; i++) slots[i] = createIngredientSlot(i, SLOT_SIZE);
        var result = createOutputSlot(0, OUTPUT_SLOT_SIZE);
        return ArsNouveauCanvasFactory.createGlyphCanvas(slots, cells, workstationIcon, result, expLabel, this::getSlotTooltip);
    }

    private Component getSlotTooltip(int index) {
        return Component.translatable("viscript_recipe.editor.ars_nouveau.ingredient_slot",
                Component.translatable("viscript_recipe.editor.ars_nouveau.glyph_input"));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        var data = getData();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.ars_nouveau"),
                intField("viscript_recipe.config.ars_nouveau.glyph.exp",
                        data.getExp(), 0, Integer.MAX_VALUE, data::setExp, this::updateExpLabel));
    }

    void updateExpLabel() {
        expLabel.setText(Component.translatable("viscript_recipe.editor.ars_nouveau.glyph_exp", getData().getExp()));
    }
}
