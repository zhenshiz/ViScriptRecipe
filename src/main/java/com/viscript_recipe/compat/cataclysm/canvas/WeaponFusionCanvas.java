package com.viscript_recipe.compat.cataclysm.canvas;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.viscript_recipe.compat.cataclysm.data.CataclysmWeaponFusionRecipeData;
import com.viscript_recipe.data.RecipeEntry;
import com.viscript_recipe.gui.canvas.RecipeCanvas;
import com.viscript_recipe.gui.views.NavigationView;

public class WeaponFusionCanvas extends RecipeCanvas<CataclysmWeaponFusionRecipeData> {
    public WeaponFusionCanvas(NavigationView navigationView, RecipeEntry entry) {super(navigationView, entry);}

    @Override
    public void load() {
        var data = getData();
        loadIngredientSlot(0, data.getBase());
        loadIngredientSlot(1, data.getAddition());
        setVisualOutput(0, data.getResult());
    }

    @Override
    public void save() {
        var data = getData();
        data.setBase(getVisualIngredient(0));
        data.setAddition(getVisualIngredient(1));
        data.setResult(getVisualOutput(0).getItem());
    }

    @Override
    public UIElement createCanvas() {
        var base = createIngredientSlot(0, JEI_SLOT_SIZE);
        var addition = createIngredientSlot(1, JEI_SLOT_SIZE);
        var result = createOutputSlot(0, JEI_SLOT_SIZE);
        configureJeiOverlaySlotVisual(base);
        configureJeiOverlaySlotVisual(addition);
        configureJeiOverlaySlotVisual(result);
        base.style(style -> style.tooltips(net.minecraft.network.chat.Component.translatable(
                "viscript_recipe.editor.cataclysm.weapon_fusion.base_slot")));
        addition.style(style -> style.tooltips(net.minecraft.network.chat.Component.translatable(
                "viscript_recipe.editor.cataclysm.weapon_fusion.addition_slot")));
        return CataclysmCanvasFactory.createWeaponFusionCanvas(base, addition, result);
    }
}
