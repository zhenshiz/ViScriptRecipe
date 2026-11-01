package com.viscript_recipe.compat.jei.ae2cs;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin;
import com.viscript_recipe.compat.ae2cs.Ae2CrystalScienceRecipeFactory;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** 使用晶体科学原生 JEI 背景、动画及槽位布局，物品由可编辑幻影槽绘制。 */
public final class Ae2CrystalScienceJeiCanvas extends UIElement {
    private IRecipeLayoutDrawable<?> drawable;

    @SuppressWarnings("unchecked")
    public void setRecipe(RecipeHolder<?> recipe) {
        var runtime = LDLibJEIPlugin.jeiRuntime;
        if (runtime == null) { drawable = null; return; }
        var manager = runtime.getRecipeManager();
        var type = manager.getRecipeType(Ae2CrystalScienceRecipeFactory.type(recipe.value())).orElse(null);
        if (type == null) { drawable = null; return; }
        var category = manager.getRecipeCategory(type);
        drawable = manager.createRecipeLayoutDrawable((mezz.jei.api.recipe.category.IRecipeCategory<Object>) (Object) category,
                recipe, runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup()).orElse(null);
        if (drawable != null) drawable.setPosition(0, 0);
    }

    public boolean isAvailable() { return drawable != null; }
    public int recipeWidth() { return drawable == null ? 135 : drawable.getRect().getWidth(); }
    public int recipeHeight() { return drawable == null ? 58 : drawable.getRect().getHeight(); }

    @SuppressWarnings("removal")
    public List<Rect2i> slots(boolean input) {
        if (drawable == null) return List.of();
        return drawable.getRecipeSlotsView().getSlotViews().stream()
                .filter(slot -> slot.getRole() == (input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT))
                .filter(IRecipeSlotDrawable.class::isInstance)
                .map(IRecipeSlotDrawable.class::cast).map(IRecipeSlotDrawable::getRect).toList();
    }

    @Override
    public void drawBackgroundAdditional(GUIContext context) {
        if (drawable == null) return;
        context.pose.pushPose();
        context.pose.translate(getPositionX(), getPositionY(), 0);
        drawCategory(drawable, context);
        context.pose.popPose();
    }

    private static <R> void drawCategory(IRecipeLayoutDrawable<R> layout, GUIContext context) {
        layout.getRecipeCategory().draw(layout.getRecipe(), layout.getRecipeSlotsView(), context.graphics, -1, -1);
    }
}
