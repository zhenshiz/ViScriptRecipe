package com.viscript_recipe.compat.jei.omnisequence;

import com.atir.molecularmanipulator.crafting.MatterFabricationRecipe;
import com.atir.molecularmanipulator.crafting.MatterRecipeIndex;
import com.atir.molecularmanipulator.integration.jei.MatterFabricationJeiCategory;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.integration.xei.jei.LDLibJEIPlugin;
import com.viscript_recipe.compat.omnisequence.OmniSequenceRecipeFactory;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public final class OmniSequenceJeiCanvas extends UIElement {
    private IRecipeLayoutDrawable<MatterFabricationRecipe> drawable;

    public void setRecipe(MatterFabricationRecipe recipe, ResourceLocation id) {
        var runtime = LDLibJEIPlugin.jeiRuntime;
        if (runtime == null) return;
        var level = Minecraft.getInstance().level;
        var original = level == null ? null : MatterRecipeIndex.get(level).fabrication(id);
        if (original != null && OmniSequenceRecipeFactory.encode(recipe, Platform.getFrozenRegistry())
                .equals(OmniSequenceRecipeFactory.encode(original.value(), Platform.getFrozenRegistry()))) {
            recipe = original.value();
        }
        var manager = runtime.getRecipeManager();
        drawable = manager.createRecipeLayoutDrawable(manager.getRecipeCategory(MatterFabricationJeiCategory.TYPE),
                recipe, runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup()).orElse(null);
    }

    @Override
    public void drawBackgroundAdditional(GUIContext context) {
        super.drawBackgroundAdditional(context);
        if (drawable == null) return;
        context.graphics.pose().pushPose();
        context.graphics.pose().translate(getPositionX(), getPositionY(), 0);
        drawable.getRecipeCategory().draw(drawable.getRecipe(), drawable.getRecipeSlotsView(), context.graphics, -1, -1);
        context.graphics.pose().popPose();
    }
}
