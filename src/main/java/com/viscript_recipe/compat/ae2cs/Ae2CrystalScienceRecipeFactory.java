package com.viscript_recipe.compat.ae2cs;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

/** 通过原生序列化器读写配方，保留标签、复合材料和产物组件。 */
public final class Ae2CrystalScienceRecipeFactory {
    private Ae2CrystalScienceRecipeFactory() {}

    public static ResourceLocation type(Recipe<?> recipe) {
        var nativeType = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType());
        if (nativeType == null || !nativeType.getNamespace().equals(Ae2CrystalScienceRecipeEditorTypes.MOD_ID)) return nativeType;
        return Ae2CrystalScienceRecipeEditorTypes.id(nativeType.getPath().replaceFirst("_recipe$", ""));
    }

    public static boolean supports(Recipe<?> recipe) {
        var type = type(recipe);
        return type != null && type.getNamespace().equals(Ae2CrystalScienceRecipeEditorTypes.MOD_ID)
                && Ae2CrystalScienceRecipeEditorTypes.TYPES.contains(type.getPath());
    }

    public static Recipe<?> compile(ResourceLocation type, String json) {
        var recipe = decode(Recipe.CODEC, JsonParser.parseString(json));
        if (!supports(recipe) || !type.equals(type(recipe)))
            throw new IllegalArgumentException("AE2 Crystal Science recipe type must be " + type);
        // 粉碎配方由其构造器校验 input，未覆盖通用 getIngredients 方法。
        if (!type.getPath().equals("crystal_pulverizer") && recipe.getIngredients().isEmpty())
            throw new IllegalArgumentException("At least one input is required");
        return recipe;
    }

    public static String encode(Recipe<?> recipe, HolderLookup.Provider provider) {
        return Recipe.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, provider), recipe).getOrThrow().toString();
    }

    public static <T> T decode(Codec<T> codec, JsonElement json) {
        return codec.parse(RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry()), json).getOrThrow();
    }

    public static <T> JsonElement encode(Codec<T> codec, T value) {
        return codec.encodeStart(RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry()), value).getOrThrow();
    }

    public static String defaults(ResourceLocation type) {
        var json = JsonParser.parseString("""
                {"input_a":{"item":"minecraft:iron_ingot","count":1},
                 "result":{"id":"minecraft:gold_ingot","count":1},"energy_cost":200}
                """).getAsJsonObject();
        json.addProperty("type", type + "_recipe_serializer");
        if (type.getPath().equals("crystal_pulverizer")) json.add("input", json.remove("input_a"));
        if (type.getPath().equals("circuit_etcher")) json.addProperty("energy_cost", 3200);
        return encode(compile(type, json.toString()), Platform.getFrozenRegistry());
    }
}
