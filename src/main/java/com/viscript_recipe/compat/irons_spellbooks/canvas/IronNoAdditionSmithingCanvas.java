package com.viscript_recipe.compat.irons_spellbooks.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.irons_spellbooks.data.IronNoAdditionSmithingRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.canvas.vanilla.BasicRecipeCanvasFactory;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;

public class IronNoAdditionSmithingCanvas extends RecipeCanvas<IronNoAdditionSmithingRecipeData> {
    public IronNoAdditionSmithingCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        loadIngredientSlot(0, getData().getTemplate());
        loadIngredientSlot(1, getData().getBase());
        setVisualOutput(0, getData().getResult());
    }

    @Override
    public void save() {
        getData().setTemplate(getVisualIngredient(0))
                .setBase(getVisualIngredient(1))
                .setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        setId("irons_smithing_canvas");
        var template = input("viscript_recipe.editor.smithing.template", 0);
        var base = input("viscript_recipe.editor.smithing.base", 1);
        var output = createOutputSlot(0, OUTPUT_SLOT_SIZE);
        output.setId("irons_smithing_output");
        return BasicRecipeCanvasFactory.createSmithingCanvas(template, base, new UIElement().setDisplay(false), output);
    }

    private UIElement input(String label, int index) {
        var slot = createIngredientSlot(index, SLOT_SIZE);
        slot.setId("irons_smithing_input_" + index);
        return RecipeEditorUi.column().layout(layout -> {
            layout.width(72);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
        }).addChildren(RecipeEditorUi.label(Component.translatable(label)), slot);
    }
}
