package com.viscript_recipe.compat.jei;

import com.viscript_recipe.Config;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.jei.create.CreateJeiRecipeFilter;
import com.viscript_recipe.compat.jei.irons_spellbooks.IronSpellbooksJeiRecipeFilter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class ViScriptRecipeJeiPlugin implements IModPlugin {
    private static final String CREATE = "create";
    private static final String IRONS_SPELLBOOKS = "irons_spellbooks";

    @Override
    public ResourceLocation getPluginUid() {
        return ViScriptRecipe.id("jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        JeiShowcaseModeState.setRuntimeApplier(() -> applyRuntime(jeiRuntime));
        JeiShowcaseModeState.updateFromLocalConfig(localShowcaseMode());
    }

    @Override
    public void onRuntimeUnavailable() {
        JeiShowcaseModeState.clearRuntimeApplier();
    }

    private static void applyRuntime(IJeiRuntime jeiRuntime) {
        if (ViScriptRecipe.isModLoaded(CREATE)) {
            CreateJeiRecipeFilter.apply(jeiRuntime, JeiShowcaseModeState.isShowcaseOnly());
        }
        if (ViScriptRecipe.isModLoaded(IRONS_SPELLBOOKS)) {
            IronSpellbooksJeiRecipeFilter.apply(jeiRuntime, JeiShowcaseModeState.isShowcaseOnly());
        }
    }

    private static boolean localShowcaseMode() {
        try {
            return Config.SHOWCASE_ONLY_VISCRIPT_RECIPES.get();
        } catch (RuntimeException e) {
            return false;
        }
    }
}
