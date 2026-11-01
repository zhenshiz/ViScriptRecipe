package com.viscript_recipe.compat.jei.neoecoae;

import cn.dancingsnow.neoecoae.recipe.IntegratedWorkingStationRecipe;
import cn.dancingsnow.neoecoae.recipe.LargeIntegratedWorkingStationRecipe;
import cn.dancingsnow.neoecoae.recipe.LargeWorkstationRecipe;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin;
import com.viscript_recipe.compat.neoecoae.NeoEcoRecipeFactory;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

/** 绘制原生 JEI 配方、槽位、流体、动画及参数，编辑事件由画布承接。 */
public final class NeoEcoJeiCanvas extends UIElement {
    private IRecipeLayoutDrawable<?> drawable;

    @SuppressWarnings("unchecked")
    public void setRecipe(RecipeHolder<?> holder, List<SizedIngredient> inputs) {
        var runtime = LDLibJEIPlugin.jeiRuntime;
        if (runtime == null) { clear(); return; }
        var manager = runtime.getRecipeManager();
        var type = manager.getRecipeType(NeoEcoRecipeFactory.type(holder.value())).orElse(null);
        if (type == null) { clear(); return; }
        Object displayed = holder;
        var station = holder.value() instanceof LargeIntegratedWorkingStationRecipe large ? large.display()
                : holder.value() instanceof IntegratedWorkingStationRecipe small ? small : null;
        if (station != null) {
            // 保留编辑中的空槽位置；保存仍由原生 Codec 输出紧凑材料列表。
            var layoutRecipe = new IntegratedWorkingStationRecipe(inputs, station.inputFluid(), station.itemOutput(),
                    station.fluidOutput(), station.energy(), station.processingTicks());
            displayed = holder.value() instanceof LargeIntegratedWorkingStationRecipe
                    ? new LargeWorkstationRecipe(holder.id(), layoutRecipe, station.energy(), List.of())
                    : new RecipeHolder<>(holder.id(), layoutRecipe);
        }
        drawable = manager.createRecipeLayoutDrawable((mezz.jei.api.recipe.category.IRecipeCategory<Object>) (Object) manager.getRecipeCategory(type),
                displayed, runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup()).orElse(null);
        if (drawable != null) drawable.setPosition(0, 0);
    }

    public void clear() { drawable = null; }
    public boolean isAvailable() { return drawable != null; }
    public int recipeHeight() { return drawable == null ? 96 : drawable.getRect().getHeight(); }

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
