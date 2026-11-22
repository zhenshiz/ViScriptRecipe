package com.viscript_recipe.compat.forbidden_arcanus;

import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.data.NativeIngredientData;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Ingredient;

public final class ForbiddenArcanusRecipeValues {
    private ForbiddenArcanusRecipeValues() {}

    public static Ingredient requireIngredient(NativeIngredientData data, HolderLookup.Provider provider) {
        var ingredient = data.compile(provider);
        if (ingredient.isEmpty()) throw new IllegalArgumentException("Missing required ingredient");
        return ingredient;
    }

    public static <T> String encode(Codec<T> codec, HolderLookup.Provider provider, T value) {
        var json = codec.encodeStart(RegistryOps.create(JsonOps.INSTANCE, provider), value).getOrThrow();
        return json instanceof JsonPrimitive primitive && primitive.isString() ? primitive.getAsString() : json.toString();
    }

    public static <T> T decode(Codec<T> codec, HolderLookup.Provider provider, String value) {
        var json = value.startsWith("{") || value.startsWith("[") ? JsonParser.parseString(value) : new JsonPrimitive(value);
        return codec.parse(RegistryOps.create(JsonOps.INSTANCE, provider), json).getOrThrow();
    }
}
