package com.viscript_recipe.compat.draconic_evolution;

import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscript_recipe.compat.draconic_evolution.data.DraconicFusionIngredientData;
import com.viscript_recipe.compat.draconic_evolution.data.DraconicFusionRecipeData;
import com.viscript_recipe.compat.draconic_evolution.data.DraconicIngredientData;

/** 必须在 RPC 扫描前注册；通过可选模组检查，避免未安装龙之进化时加载原生 TechLevel。 */
public final class DraconicEvolutionRecipeAccessors {
    private DraconicEvolutionRecipeAccessors() {}

    @ViScriptRegisterAccessors(modId = DraconicEvolutionRecipeEditorTypes.MOD_ID)
    public static void register(RegisterAccessorEvent event) {
        event.register(DraconicIngredientData.class, DraconicIngredientData::new);
        event.register(DraconicFusionIngredientData.class, DraconicFusionIngredientData::new);
        event.register(DraconicFusionRecipeData.class, DraconicFusionRecipeData::new);
    }
}
