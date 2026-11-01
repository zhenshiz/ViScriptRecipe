package com.viscript_recipe.compat.powah;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import owmii.powah.block.energizing.EnergizingRecipe;

public final class PowahRecipeFactory {
    private PowahRecipeFactory() {}

    public static EnergizingRecipe compile(ResourceLocation type, String json) {
        var definition = JsonParser.parseString(json).getAsJsonObject();
        var recipe = decode(Recipe.CODEC, definition);
        if (!PowahRecipeEditorTypes.ENERGIZING.equals(type) || !(recipe instanceof EnergizingRecipe energizing))
            throw new IllegalArgumentException("Expected a Powah energizing recipe");
        if (energizing.getIngredients().isEmpty() || energizing.getIngredients().size() > 6)
            throw new IllegalArgumentException("Energizing requires between 1 and 6 ingredients");
        if (energizing.getResultItem().isEmpty()) throw new IllegalArgumentException("Energizing requires an output");
        // ItemStack Codec 会把无效数量回退到 1；发布时不能静默改变玩家填写的数量。
        var result = definition.getAsJsonObject("result");
        if (result.has("count") && result.get("count").getAsLong() != energizing.getResultItem().getCount())
            throw new IllegalArgumentException("Output count is not supported by the native item codec");
        if (energizing.getEnergy() < 1) throw new IllegalArgumentException("Energy must be positive");
        return energizing;
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
}
