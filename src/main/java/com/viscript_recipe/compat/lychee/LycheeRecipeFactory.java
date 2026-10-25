package com.viscript_recipe.compat.lychee;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.util.recipe.ILycheeRecipe;

/** 使用原生配方 Codec 保留条件、动作、引用和原料扩展，不复制 Lychee 的配方执行器。 */
public final class LycheeRecipeFactory {
    public static final Gson JSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private LycheeRecipeFactory() {}

    public static Recipe<?> compile(ResourceLocation type, String json) {
        JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        if (!object.has("type") || !type.toString().equals(object.get("type").getAsString())) {
            throw new IllegalArgumentException("Lychee recipe type must be " + type);
        }
        Recipe<?> recipe = Recipe.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry()), object).getOrThrow();
        if (!(recipe instanceof ILycheeRecipe<?>)) throw new IllegalArgumentException("Not a Lychee recipe: " + type);
        return recipe;
    }

    public static String encode(Recipe<?> recipe, HolderLookup.Provider provider) {
        return JSON.toJson(Recipe.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, provider), recipe).getOrThrow());
    }

    public static ResourceLocation type(Recipe<?> recipe) {
        return BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer());
    }

    /** 使用原生网络序列化器，遵守 Lychee 对 preventSync 动作及客户端数据的处理。 */
    public static byte[] encodeForSync(HolderLookup.Provider provider, RecipeHolder<?> recipe) {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), (RegistryAccess) provider);
        try {
            RecipeHolder.STREAM_CODEC.encode(buffer, recipe);
            var bytes = new byte[buffer.readableBytes()];
            buffer.readBytes(bytes);
            return bytes;
        } finally { buffer.release(); }
    }

    public static RecipeHolder<?> decodeFromSync(HolderLookup.Provider provider, byte[] bytes) {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(bytes), (RegistryAccess) provider);
        try { return RecipeHolder.STREAM_CODEC.decode(buffer); }
        finally { buffer.release(); }
    }

    public static String defaults(ResourceLocation type) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type.toString());
        String fields = switch (type.getPath()) {
            case "block_interacting", "block_clicking" -> "\"item_in\":\"minecraft:iron_ingot\",\"block_in\":\"minecraft:stone\"";
            case "item_burning", "item_exploding", "lightning_channeling", "blank" -> "\"item_in\":\"minecraft:iron_ingot\"";
            case "item_inside" -> "\"item_in\":\"minecraft:iron_ingot\",\"block_in\":\"minecraft:water\",\"time\":20";
            case "anvil_crafting" -> "\"item_in\":[{\"item\":\"minecraft:iron_ingot\"},{\"item\":\"minecraft:gold_ingot\"}],\"item_out\":\"minecraft:diamond\"";
            case "block_crushing" -> "\"item_in\":\"minecraft:iron_ingot\",\"falling_block\":\"minecraft:anvil\"";
            case "block_exploding", "random_block_ticking" -> "\"block_in\":\"minecraft:cobblestone\"";
            case "dripstone_dripping" -> "\"source_block\":\"minecraft:water\",\"target_block\":\"minecraft:cauldron\"";
            case "crafting" -> "\"pattern\":[\"A\"],\"key\":{\"A\":{\"item\":\"minecraft:iron_ingot\"}},\"result\":{\"id\":\"minecraft:diamond\",\"count\":1}";
            case "entity_ticking" -> "\"entity\":{\"type\":\"minecraft:pig\"},\"interval\":20";
            default -> throw new IllegalArgumentException("Unsupported Lychee editor type: " + type);
        };
        JsonParser.parseString("{" + fields + "}").getAsJsonObject().entrySet().forEach(e -> json.add(e.getKey(), e.getValue()));
        if (!type.getPath().equals("crafting") && !type.getPath().equals("anvil_crafting")) {
            json.add("post", JsonParser.parseString("[{\"type\":\"drop_item\",\"id\":\"minecraft:diamond\",\"count\":1}]"));
        }
        if (type.getPath().equals("blank")) json.addProperty("ghost", true);
        return encode(compile(type, json.toString()), Platform.getFrozenRegistry());
    }
}
