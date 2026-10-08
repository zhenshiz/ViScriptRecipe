package com.viscript_recipe.compat.bakery;

import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscript_recipe.compat.bakery.data.BakeryBakingStationRecipeData;
import com.viscript_recipe.compat.bakery.data.BakeryCakeInteractionRecipeData;

/** 通过 LDLib2 数据访问器持久化 Bakery 配方。 */
public final class BakeryRecipeAccessors {
    private BakeryRecipeAccessors() {}

    /**
     * 为 Bakery 配方数据注册 LDLib2 持久化访问器。
     * @param event 数据访问器注册事件
     */
    @ViScriptRegisterAccessors(modId = BakeryRecipeEditorTypes.MOD_ID)
    public static void register(RegisterAccessorEvent event) {
        event.register(BakeryBakingStationRecipeData.class, BakeryBakingStationRecipeData::new);
        event.register(BakeryCakeInteractionRecipeData.class, BakeryCakeInteractionRecipeData::new);
    }
}
