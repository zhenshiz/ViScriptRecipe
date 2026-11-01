package com.viscript_recipe.compat.jei.ae2lt;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin;
import com.viscript_recipe.compat.ae2lt.Ae2LightningRecipeFactory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

/** 使用闪电科技已注册的 JEI 分类绘制背景、动画、槽位与结构预览。 */
public final class Ae2LightningJeiCanvas extends UIElement {
    private IRecipeLayoutDrawable<?> drawable;
    private boolean valid;

    @SuppressWarnings("unchecked")
    public void setRecipe(Recipe<?> recipe) {
        valid = true;
        var runtime = LDLibJEIPlugin.jeiRuntime;
        if (runtime == null) { drawable = null; return; }
        var manager = runtime.getRecipeManager();
        var type = manager.getRecipeType(Ae2LightningRecipeFactory.type(recipe)).orElse(null);
        if (type == null) { drawable = null; return; }
        var category = manager.getRecipeCategory(type);
        drawable = manager.createRecipeLayoutDrawable((mezz.jei.api.recipe.category.IRecipeCategory<Object>) (Object) category,
                recipe, runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup()).orElse(null);
        if (drawable != null) drawable.setPosition(0, 0);
    }

    public boolean isAvailable() { return drawable != null; }
    public boolean drawsIngredients() { return valid && drawable != null; }
    public void invalidateRecipe() { valid = false; }
    public int recipeWidth() { return drawable == null ? 176 : drawable.getRect().getWidth(); }
    public int recipeHeight() { return drawable == null ? 90 : drawable.getRect().getHeight(); }

    @SuppressWarnings("removal")
    public List<Rect2i> slots(boolean input, boolean fluid) {
        if (drawable == null) return List.of();
        return drawable.getRecipeSlotsView().getSlotViews().stream()
                .filter(slot -> slot.getRole() == (input ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT))
                .filter(slot -> fluid ? slot.getIngredients(NeoForgeTypes.FLUID_STACK).findAny().isPresent()
                        : slot.getIngredients(VanillaTypes.ITEM_STACK).findAny().isPresent())
                .filter(IRecipeSlotDrawable.class::isInstance)
                .map(IRecipeSlotDrawable.class::cast).map(IRecipeSlotDrawable::getRect).toList();
    }

    @Override
    public void screenTick() {
        super.screenTick();
        if (drawable != null) drawable.tick();
    }

    @Override
    public void drawBackgroundAdditional(GUIContext context) {
        if (drawable == null) return;
        context.pose.pushPose();
        context.pose.translate(getPositionX(), getPositionY(), 0);
        if (valid) drawable.drawRecipe(context.graphics, (int) context.localMouseX, (int) context.localMouseY);
        else drawCategory(drawable, context);
        context.pose.popPose();
    }
    @SuppressWarnings("removal")
    private static <R> void drawCategory(IRecipeLayoutDrawable<R> layout, GUIContext context) {
        var category = layout.getRecipeCategory();
        var background = category.getBackground();
        if (background != null) background.draw(context.graphics);
        category.draw(layout.getRecipe(), layout.getRecipeSlotsView(), context.graphics, -1, -1);
    }
}
