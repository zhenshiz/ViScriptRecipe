package com.viscript_recipe.compat.jei;

import com.viscript_recipe.Config;
import com.viscript_recipe.ViScriptRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class ViScriptRecipeJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ViScriptRecipe.id("jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        RecipeDeltaJeiSynchronizer.onRuntimeAvailable(jeiRuntime);
        JeiShowcaseModeState.setRuntimeApplier(() ->
                JeiRecipeCompatibility.applyShowcaseFilters(jeiRuntime, JeiShowcaseModeState.isShowcaseOnly()));
        JeiShowcaseModeState.updateFromLocalConfig(localShowcaseMode());
    }

    @Override
    public void onRuntimeUnavailable() {
        RecipeDeltaJeiSynchronizer.onRuntimeUnavailable();
        JeiShowcaseModeState.clearRuntimeApplier();
    }

    private static boolean localShowcaseMode() {
        try {
            return Config.SHOWCASE_ONLY_VISCRIPT_RECIPES.get();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
