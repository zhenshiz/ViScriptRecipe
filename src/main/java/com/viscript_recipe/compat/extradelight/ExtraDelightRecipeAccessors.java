package com.viscript_recipe.compat.extradelight;

import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscript_recipe.compat.extradelight.data.*;

/** 注册 Extra Delight 的 LDLib2 配方数据访问器。 */
public final class ExtraDelightRecipeAccessors {
    private ExtraDelightRecipeAccessors() {}

    @ViScriptRegisterAccessors(modId = ExtraDelightRecipeEditorTypes.MOD_ID)
    public static void register(RegisterAccessorEvent event) {
        event.register(ExtraDelightMortarRecipeData.class, ExtraDelightMortarRecipeData::new);
        event.register(ExtraDelightMixingBowlRecipeData.class, ExtraDelightMixingBowlRecipeData::new);
        event.register(ExtraDelightOvenRecipeData.class, ExtraDelightOvenRecipeData::new);
        event.register(ExtraDelightDryingRackRecipeData.class, ExtraDelightDryingRackRecipeData::new);
        event.register(ExtraDelightDoughShapingRecipeData.class, ExtraDelightDoughShapingRecipeData::new);
        event.register(ExtraDelightToolOnBlockRecipeData.class, ExtraDelightToolOnBlockRecipeData::new);
        event.register(ExtraDelightFeastRecipeData.class, ExtraDelightFeastRecipeData::new);
        event.register(ExtraDelightMeltingPotRecipeData.class, ExtraDelightMeltingPotRecipeData::new);
        event.register(ExtraDelightChillerRecipeData.class, ExtraDelightChillerRecipeData::new);
        event.register(ExtraDelightVatRecipeData.class, ExtraDelightVatRecipeData::new);
        event.register(ExtraDelightEvaporatorRecipeData.class, ExtraDelightEvaporatorRecipeData::new);
        event.register(ExtraDelightBottleFluidRecipeData.class, ExtraDelightBottleFluidRecipeData::new);
        event.register(ExtraDelightShapedJarRecipeData.class, ExtraDelightShapedJarRecipeData::new);
        event.register(ExtraDelightJuicerRecipeData.class, ExtraDelightJuicerRecipeData::new);
        event.register(ExtraDelightDynamicJamRecipeData.class, ExtraDelightDynamicJamRecipeData::new);
        event.register(ExtraDelightDynamicToastRecipeData.class, ExtraDelightDynamicToastRecipeData::new);
        event.register(ExtraDelightVatStageData.class, ExtraDelightVatStageData::new);
    }
}
