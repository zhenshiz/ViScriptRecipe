package com.viscript_recipe.compat.irons_spellbooks;

import com.viscript_recipe.ViScriptRecipe;
import io.redspace.ironsspellbooks.config.ServerConfigs;

public final class IronSpellbooksRecipeUiSupport {
    private IronSpellbooksRecipeUiSupport() {
    }

    public static int scrollRecycleChancePercent() {
        if (!ViScriptRecipe.isModLoaded("irons_spellbooks")) {
            return 0;
        }
        var chance = Math.clamp(ServerConfigs.SCROLL_RECYCLE_CHANCE.get(), 0, 1);
        return (int) (chance * 100);
    }
}
