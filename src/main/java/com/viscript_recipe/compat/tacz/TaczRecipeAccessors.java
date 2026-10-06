package com.viscript_recipe.compat.tacz;

import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscript_recipe.compat.tacz.data.TaczIngredientData;
import com.viscript_recipe.compat.tacz.data.TaczRecipeData;

/** 仅在安装 TACZ 时注册其配方数据访问器。 */
public final class TaczRecipeAccessors {
    private TaczRecipeAccessors() {}

    /**
     * 注册工作台配方及材料的 LDLib2 持久化访问器。
     *
     * @param event 访问器注册事件
     */
    @ViScriptRegisterAccessors(modId = TaczRecipeEditorTypes.MOD_ID)
    public static void register(RegisterAccessorEvent event) {
        event.register(TaczIngredientData.class, TaczIngredientData::new);
        event.register(TaczRecipeData.class, TaczRecipeData::new);
    }
}
