package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.viscript_recipe.data.vanilla.VanillaRecipeDataAccessors;

import java.util.function.Supplier;

public final class RecipeDataAccessors {

    public static synchronized void register() {
        registerType(RecipeFile.class, RecipeFile::new);
        registerType(RecipeEntry.class, RecipeEntry::new);
        registerType(RecipeIngredient.class, RecipeIngredient::new);
        registerType(RecipeIngredientValue.class, RecipeIngredientValue::new);
        VanillaRecipeDataAccessors.register();
    }

    public static <T> void registerType(Class<T> type, Supplier<T> factory) {
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(type)
                .codec(PersistedParser.createCodec(factory))
                .streamCodec(PersistedParser.createStreamCodec(factory))
                .codecMark()
                .build(), 0);
    }
}
