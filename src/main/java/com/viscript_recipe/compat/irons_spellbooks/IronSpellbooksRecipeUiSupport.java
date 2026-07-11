package com.viscript_recipe.compat.irons_spellbooks;

import io.redspace.ironsspellbooks.config.ServerConfigs;
import net.neoforged.fml.ModList;

public final class IronSpellbooksRecipeUiSupport {
    private IronSpellbooksRecipeUiSupport() {
    }

    public static int scrollRecycleChancePercent() {
        if (!ModList.get().isLoaded("irons_spellbooks")) {
            return 0;
        }
        return IronSpellbooksRecipeUiSupportImpl.scrollRecycleChancePercent();
    }
}

final class IronSpellbooksRecipeUiSupportImpl {
    private IronSpellbooksRecipeUiSupportImpl() {
    }

    static int scrollRecycleChancePercent() {
        var chance = Math.max(0, Math.min(1, ServerConfigs.SCROLL_RECYCLE_CHANCE.get()));
        return (int) (chance * 100);
    }
}
