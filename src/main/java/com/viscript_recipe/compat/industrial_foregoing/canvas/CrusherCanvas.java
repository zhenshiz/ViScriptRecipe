package com.viscript_recipe.compat.industrial_foregoing.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.industrial_foregoing.data.IndustrialCrusherRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CrusherCanvas extends RecipeCanvas<IndustrialCrusherRecipeData> {
    public CrusherCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getInput());
        loadIngredientSlot(1, data.getOutput());
    }

    @Override
    public void save() {
        getData().setInput(getVisualIngredient(0)).setOutput(getVisualIngredient(1));
    }

    @Override
    public UIElement createCanvas() {
        var input = createIngredientSlot(0, 18);
        var output = createIngredientSlot(1, 18);
        configureJeiOverlaySlotVisual(input, output);
        var action = createItemIcon(new ItemStack(Items.DIAMOND_PICKAXE), 18);
        tooltip(action, Component.translatable("viscript_recipe.editor.industrial_foregoing.crusher.action"));
        return IndustrialForegoingCanvasFactory.createCrusher(
                IndustrialForegoingCanvasFactory.slotCell(input, 18, 18),
                IndustrialForegoingCanvasFactory.slotCell(action, 18, 18),
                IndustrialForegoingCanvasFactory.slotCell(output, 18, 18));
    }
}
