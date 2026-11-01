package com.viscript_recipe.compat.jei.powah;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin;
import com.viscript_recipe.ViScriptRecipe;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import net.minecraft.world.item.crafting.RecipeHolder;
import owmii.powah.block.energizing.EnergizingRecipe;
import owmii.powah.compat.jei.JeiEnergizingCategory;

/** 绘制 Powah 原生 JEI 背景、材料候选项、产物和 FE 文本。 */
public final class PowahJeiCanvas extends UIElement {
    private IRecipeLayoutDrawable<?> drawable;

    public void setRecipe(EnergizingRecipe recipe) {
        var runtime = LDLibJEIPlugin.jeiRuntime;
        if (runtime == null) { drawable = null; return; }
        var manager = runtime.getRecipeManager();
        drawable = manager.createRecipeLayoutDrawable(manager.getRecipeCategory(JeiEnergizingCategory.TYPE.get()),
                new RecipeHolder<>(ViScriptRecipe.id("powah_preview"), recipe),
                runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup()).orElse(null);
        if (drawable != null) drawable.setPosition(0, 0);
    }

    public boolean isAvailable() { return drawable != null; }

    @Override
    public void screenTick() { super.screenTick(); if (drawable != null) drawable.tick(); }

    @Override
    public void drawBackgroundAdditional(GUIContext context) {
        if (drawable == null) return;
        context.pose.pushPose();
        context.pose.translate(getPositionX(), getPositionY(), 0);
        drawable.drawRecipe(context.graphics, -1, -1);
        context.pose.popPose();
    }
}
