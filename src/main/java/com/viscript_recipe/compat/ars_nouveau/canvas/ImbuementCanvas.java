package com.viscript_recipe.compat.ars_nouveau.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.ars_nouveau.data.ArsNouveauImbuementRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import com.viscript_recipe.recipe.RecipeHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import static com.viscript_recipe.compat.ars_nouveau.canvas.ApparatusCanvas.*;

public class ImbuementCanvas extends RecipeCanvas<ArsNouveauImbuementRecipeData> {
    public ImbuementCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getInput());
        loadIngredients(data.getPedestalItems(), 1);
        setVisualOutput(0, data.getResult());
        updateSourceLabel(data.getSource());
    }

    @Override
    public void save() {
        var data = getData();
        data.setInput(getVisualIngredient(0));
        data.setPedestalItems(getIngredients(3, 1, true));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var input = createIngredientSlot(0, SLOT_SIZE);
        var pedestals = new UIElement[3];
        var cells = new UIElement[3];
        for (int i = 0; i < pedestals.length; i++) pedestals[i] = createIngredientSlot(i + 1, SLOT_SIZE);
        var result = createOutputSlot(0, OUTPUT_SLOT_SIZE);
        return ArsNouveauCanvasFactory.createImbuementCanvas(input, pedestals, cells,
                createItemIcon(RecipeHelper.registryItem("ars_nouveau:imbuement_chamber", Items.ENCHANTING_TABLE), 32),
                result, sourceLabel, this::getSlotTooltip);
    }

    private Component getSlotTooltip(int index) {
        return Component.translatable("viscript_recipe.editor.ars_nouveau.ingredient_slot",
                Component.translatable(index == 0 ? "viscript_recipe.editor.ars_nouveau.input" :
                        "viscript_recipe.editor.ars_nouveau.pedestal_item"));
    }

    @Override
    public void buildRecipeProperties(UIElement content) {
        content.addChild(sectionTitle("viscript_recipe.editor.properties.ars_nouveau"));
        addSourceField(content, getData().getSource(), getData()::setSource);
    }
}
