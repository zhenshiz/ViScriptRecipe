package com.viscript_recipe.compat.enderio;

import com.viscript_lib.annotation.ViScriptRegisterAccessors;
import com.viscript_lib.event.RegisterAccessorEvent;
import com.viscript_recipe.compat.enderio.data.*;

public final class EnderIoRecipeAccessors {
    private EnderIoRecipeAccessors() {}

    @ViScriptRegisterAccessors(modId = EnderIoRecipeEditorTypes.MOD_ID)
    public static void register(RegisterAccessorEvent event) {
        event.register(EnderIoIngredientData.class, EnderIoIngredientData::new);
        event.register(EnderIoOutputData.class, EnderIoOutputData::new);
        event.register(EnderIoFluidData.class, EnderIoFluidData::new);
        event.register(EnderIoRecipeData.class, EnderIoRecipeData::new);
    }
}
