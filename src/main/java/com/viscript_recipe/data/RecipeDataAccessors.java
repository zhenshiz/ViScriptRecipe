package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.viscript_recipe.data.ars_nouveau.ArsNouveauRecipeDataAccessors;
import com.viscript_recipe.data.avaritia.AvaritiaRecipeDataAccessors;
import com.viscript_recipe.data.create.CreateRecipeDataAccessors;
import com.viscript_recipe.data.iceandfire.IceAndFireRecipeDataAccessors;
import com.viscript_recipe.data.extendedcrafting.ExtendedCraftingRecipeDataAccessors;
import com.viscript_recipe.data.farmersdelight.FarmersDelightRecipeDataAccessors;
import com.viscript_recipe.data.irons_spellbooks.IronSpellbooksRecipeDataAccessors;
import com.viscript_recipe.data.kaleidoscope_cookery.KaleidoscopeCookeryRecipeDataAccessors;
import com.viscript_recipe.data.vanilla.VanillaRecipeDataAccessors;

import java.util.function.Supplier;

public final class RecipeDataAccessors {

    public static synchronized void register() {
        registerType(RecipeFile.class, RecipeFile::new);
        registerType(RecipeEntry.class, RecipeEntry::new);
        registerType(RecipeIngredient.class, RecipeIngredient::new);
        registerType(RecipeIngredientValue.class, RecipeIngredientValue::new);
        VanillaRecipeDataAccessors.register();
        IronSpellbooksRecipeDataAccessors.register();
        IceAndFireRecipeDataAccessors.register();
        FarmersDelightRecipeDataAccessors.register();
        CreateRecipeDataAccessors.register();
        ExtendedCraftingRecipeDataAccessors.register();
        ArsNouveauRecipeDataAccessors.register();
        KaleidoscopeCookeryRecipeDataAccessors.register();
        AvaritiaRecipeDataAccessors.register();
    }

    public static <T> void registerType(Class<T> type, Supplier<T> factory) {
        AccessorRegistries.registerAccessor(CustomDirectAccessor.builder(type)
                .codec(PersistedParser.createCodec(factory))
                .streamCodec(PersistedParser.createStreamCodec(factory))
                .codecMark()
                .build(), 0);
    }
}
