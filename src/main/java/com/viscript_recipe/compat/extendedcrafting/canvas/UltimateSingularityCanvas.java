package com.viscript_recipe.compat.extendedcrafting.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.extendedcrafting.data.ExtendedCraftingUltimateSingularityRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.editor.RecipeEditorUi;
import com.viscript_recipe.gui.views.NavigationView;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;

public class UltimateSingularityCanvas extends RecipeCanvas<ExtendedCraftingUltimateSingularityRecipeData> {
    public UltimateSingularityCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        setVisualOutput(0, getData().getResult());
    }

    @Override
    public void save() {
        getData().setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        return RecipeEditorUi.column().layout(layout -> {
            layout.widthPercent(100);
            layout.flex(1);
            layout.gapAll(4);
            layout.alignItems(AlignItems.CENTER);
            layout.justifyContent(AlignContent.CENTER);
        }).addChildren(RecipeEditorUi.label(Component.translatable("viscript_recipe.editor.result")), createOutputSlot(0, OUTPUT_SLOT_SIZE));
    }
}
