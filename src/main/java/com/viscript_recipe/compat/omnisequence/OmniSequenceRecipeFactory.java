package com.viscript_recipe.compat.omnisequence;

import com.atir.molecularmanipulator.crafting.MatterFabricationRecipe;
import com.atir.molecularmanipulator.crafting.MatterMachineImportRecipe;
import com.atir.molecularmanipulator.research.MatterResearchRecipe;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

/** 使用原生 Codec 校验和保存定义，保留扩展 AE 资源与原料条件。 */
public final class OmniSequenceRecipeFactory {
    private OmniSequenceRecipeFactory() {}

    public static boolean supports(Recipe<?> recipe) {
        return recipe instanceof MatterFabricationRecipe || recipe instanceof MatterResearchRecipe || recipe instanceof MatterMachineImportRecipe;
    }

    public static ResourceLocation type(Recipe<?> recipe) {
        return BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer());
    }

    public static Recipe<?> compile(ResourceLocation type, String json) {
        var object = JsonParser.parseString(json).getAsJsonObject();
        if (!object.has("type") || !type.toString().equals(object.get("type").getAsString()))
            throw new IllegalArgumentException("OmniSequence recipe type must be " + type);
        var recipe = decode(Recipe.CODEC, object);
        if (!supports(recipe)) throw new IllegalArgumentException("Not an OmniSequence recipe: " + type);
        return recipe;
    }

    public static String encode(Recipe<?> recipe, HolderLookup.Provider provider) {
        return Recipe.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, provider), recipe).getOrThrow().toString();
    }

    public static <T> T decode(Codec<T> codec, com.google.gson.JsonElement json) {
        return codec.parse(RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry()), json).getOrThrow();
    }

    public static <T> com.google.gson.JsonElement encode(Codec<T> codec, T value) {
        return codec.encodeStart(RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry()), value).getOrThrow();
    }

    public static JsonObject defaultCost() {
        return JsonParser.parseString("{\"ingredient\":{\"item\":\"minecraft:iron_ingot\"},\"count\":1}").getAsJsonObject();
    }

    public static JsonObject defaultResearch() {
        return JsonParser.parseString("""
                {"title":"viscript_recipe.editor.omnisequence.default_research","ingredients":[{"ingredient":{"item":"minecraft:iron_ingot"},"count":1}],
                 "duration":1200,"ae_per_tick":256,"stage":1}
                """).getAsJsonObject();
    }

    public static String defaults(ResourceLocation type) {
        var object = switch (type.getPath()) {
            case "matter_fabrication" -> JsonParser.parseString("""
                    {"ingredients":[{"ingredient":{"item":"minecraft:iron_ingot"},"count":1}],
                     "results":[{"id":"minecraft:gold_ingot","count":1}],"processing_time":400,"ae_per_tick":64}
                    """).getAsJsonObject();
            case "matter_research" -> defaultResearch();
            case "matter_machine_import" -> {
                var declaration = JsonParser.parseString("""
                        {"machine":"minecraft:furnace","serializers":["minecraft:smelting"],
                         "processing_time":400,"ae_per_tick":256}
                        """).getAsJsonObject();
                declaration.add("research", defaultResearch());
                yield declaration;
            }
            default -> throw new IllegalArgumentException("Unsupported OmniSequence editor type: " + type);
        };
        object.addProperty("type", type.toString());
        return encode(compile(type, object.toString()), Platform.getFrozenRegistry());
    }
}
