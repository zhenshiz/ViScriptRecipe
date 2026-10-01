package com.viscript_recipe.compat.eidolon;

import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscript_recipe.compat.eidolon.data.*;

public final class EidolonRecipeAccessors {
    private EidolonRecipeAccessors() {}

    @ViScriptRegisterAccessors(modId = EidolonRecipeEditorTypes.MOD_ID)
    public static void register(RegisterAccessorEvent event) {
        event.register(EidolonIngredientValueData.class, EidolonIngredientValueData::new);
        event.register(EidolonIngredientData.class, EidolonIngredientData::new);
        event.register(EidolonCrucibleStepData.class, EidolonCrucibleStepData::new);
        event.register(EidolonCrucibleRecipeData.class, EidolonCrucibleRecipeData::new);
        event.register(EidolonWorktableRecipeData.class, EidolonWorktableRecipeData::new);
        event.register(EidolonDyeRecipeData.class, EidolonDyeRecipeData::new);
        event.register(EidolonRitualRecipeData.class, EidolonRitualRecipeData::new);
    }
}
