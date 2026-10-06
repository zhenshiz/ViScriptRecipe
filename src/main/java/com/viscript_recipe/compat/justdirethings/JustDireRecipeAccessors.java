package com.viscript_recipe.compat.justdirethings;

import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscript_recipe.compat.justdirethings.data.*;

/** 仅在安装 Just Dire Things 时注册持久化访问器。 */
public final class JustDireRecipeAccessors {
    private JustDireRecipeAccessors() {}

    /**
     * 注册配方数据及复杂材料。
     * @param event LDLib2 访问器注册事件
     */
    @ViScriptRegisterAccessors(modId = JustDireRecipeEditorTypes.MOD_ID)
    public static void register(RegisterAccessorEvent event) {
        event.register(JustDireTransformationData.class, JustDireTransformationData::new);
        event.register(JustDireSmithingData.class, JustDireSmithingData::new);
        event.register(JustDireIngredientData.class, JustDireIngredientData::new);
    }
}
