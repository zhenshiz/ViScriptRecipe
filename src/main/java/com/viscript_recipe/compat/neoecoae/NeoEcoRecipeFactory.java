package com.viscript_recipe.compat.neoecoae;

import cn.dancingsnow.neoecoae.recipe.CoolingRecipe;
import cn.dancingsnow.neoecoae.recipe.IntegratedWorkingStationRecipe;
import cn.dancingsnow.neoecoae.recipe.LargeIntegratedWorkingStationRecipe;
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

/** 使用原生 Codec 保存物品、流体标签、复合材料和组件。 */
public final class NeoEcoRecipeFactory {
    private NeoEcoRecipeFactory() {}

    public static ResourceLocation type(Recipe<?> recipe) { return BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()); }

    public static boolean supports(Recipe<?> recipe) {
        return recipe instanceof CoolingRecipe || recipe instanceof IntegratedWorkingStationRecipe
                || recipe instanceof LargeIntegratedWorkingStationRecipe;
    }

    public static Recipe<?> compile(ResourceLocation type, String json) {
        var recipe = decode(Recipe.CODEC, JsonParser.parseString(json));
        if (!supports(recipe) || !type.equals(type(recipe))) throw new IllegalArgumentException("Neo ECO recipe type must be " + type);
        if (recipe instanceof CoolingRecipe cooling) {
            if (cooling.input().ingredient().isEmpty() || cooling.inputAmount() <= 0 || cooling.coolant() < 0 || cooling.maxOverclock() < 0)
                throw new IllegalArgumentException("Invalid cooling input or parameters");
        } else {
            var station = recipe instanceof LargeIntegratedWorkingStationRecipe large ? large.display() : (IntegratedWorkingStationRecipe) recipe;
            if (station.inputItems().isEmpty() && station.inputFluid().ingredient().isEmpty())
                throw new IllegalArgumentException("At least one input is required");
            if (!station.hasItemOutput() && !station.hasFluidOutput()) throw new IllegalArgumentException("At least one output is required");
            if (station.energy() < 0) throw new IllegalArgumentException("Energy must be non-negative");
        }
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
        var json = JsonParser.parseString(type.getPath().equals("cooling") ? """
                {"input":{"fluid":"minecraft:water","amount":1000},"output":{},"coolant":100,"max_overclock":0}
                """ : """
                {"inputItems":[{"item":"minecraft:iron_ingot","count":1}],
                 "itemOutput":{"id":"minecraft:gold_ingot","count":1},"energy":1000,"processingTicks":100}
                """).getAsJsonObject();
        json.addProperty("type", type.toString());
        return encode(compile(type, json.toString()), Platform.getFrozenRegistry());
    }
}
