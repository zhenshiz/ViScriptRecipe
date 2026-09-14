package com.viscript_recipe.compat.ars_nouveau.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.ars_nouveau.data.ArsNouveauCrushOutputData;
import com.viscript_recipe.compat.ars_nouveau.data.ArsNouveauCrushRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.gui.views.PropertiesView;
import com.viscript_recipe.recipe.RecipeHelper;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;

public class CrushCanvas extends RecipeCanvas<ArsNouveauCrushRecipeData> {
    static final int[] maxRanges = new int[]{1, 1, 1, 1, 1, 1};
    static final int outputCount = 6;

    public CrushCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        Arrays.fill(maxRanges, 1);
        var data = getData();
        loadIngredientSlot(0, data.getInput());
        for (int i = 0; i < Math.min(outputCount, data.getOutputs().size()); i++) {
            var output = data.getOutputs().get(i);
            setVisualOutput(i, output.getItem(), output.getChance());
            maxRanges[i] =output.getMaxRange();
        }
    }

    @Override
    public void save() {
        var data = getData();
        data.setInput(getVisualIngredient(0));
        var outputs = new ArrayList<ArsNouveauCrushOutputData>();
        for (int i = 0; i < outputCount; i++) {
            var output = getVisualOutput(i);
            if (!output.getItem().isEmpty()) {
                outputs.add(new ArsNouveauCrushOutputData().setItem(output.getItem())
                        .setChance(output.getChance()).setMaxRange(maxRanges[i]));
            }
        }
        data.setOutputs(outputs);
    }

    @Override
    public UIElement createCanvas() {
        var input = createIngredientSlot(0, SLOT_SIZE);
        var outputs = new UIElement[outputCount];
        var cells = new UIElement[outputCount];
        for (int i = 0; i < outputCount; i++) outputs[i] = createOutputSlot(i, SLOT_SIZE);
        return ArsNouveauCanvasFactory.createCrushCanvas(input,
                createItemIcon(RecipeHelper.registryItem("ars_nouveau:glyph_crush", Items.IRON_PICKAXE), 82),
                outputs, cells);
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.ars_nouveau.crush"),
                switchField("viscript_recipe.config.ars_nouveau.crush.skip_block_place",
                        getData().isSkipBlockPlace(), getData()::setSkipBlockPlace));
    }

    @Override
    public void buildResultProperties(UIElement content) {
        int index = selectedSlotIndex();
        content.addChildren(sectionTitle("viscript_recipe.editor.properties.ars_nouveau.output"),
                PropertiesView.createItemStackConfigurator("viscript_recipe.config.ars_nouveau.crush.output_item",
                        () -> getSelectedOutput().getItem(), this::setSelectedOutput
                ),
                floatField("viscript_recipe.config.ars_nouveau.crush.chance",
                        getSelectedOutput().getChance(), 0, 1, this::setSelectedOutput),
                intField("viscript_recipe.config.ars_nouveau.crush.max_range",
                        maxRanges[index], 1, Integer.MAX_VALUE, value -> maxRanges[index] = value)
        );
    }
}
