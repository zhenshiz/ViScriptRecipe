package com.viscript_recipe.compat.ae2lt;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.moakiee.ae2lt.machine.crystalcatalyzer.recipe.CrystalCatalyzerRecipe;
import com.moakiee.ae2lt.machine.crystalcatalyzer.recipe.CrystalHarvestOutput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

/** 通过原生序列化器编译配方，保留材料条件、组件和扩展产物声明。 */
public final class Ae2LightningRecipeFactory {
    private Ae2LightningRecipeFactory() {}

    public static ResourceLocation type(Recipe<?> recipe) {
        return BuiltInRegistries.RECIPE_SERIALIZER.getKey(recipe.getSerializer());
    }

    public static boolean supports(Recipe<?> recipe) {
        var type = type(recipe);
        return type != null && type.getNamespace().equals(Ae2LightningRecipeEditorTypes.MOD_ID)
                && Ae2LightningRecipeEditorTypes.TYPES.contains(type.getPath());
    }

    public static Recipe<?> compile(ResourceLocation type, String json) {
        var object = JsonParser.parseString(json).getAsJsonObject();
        if (!object.has("type") || !type.toString().equals(object.get("type").getAsString()))
            throw new IllegalArgumentException("AE2 Lightning Tech recipe type must be " + type);
        var recipe = decode(Recipe.CODEC, object);
        if (!supports(recipe)) throw new IllegalArgumentException("Unsupported AE2 Lightning Tech recipe: " + type);
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

    /**
     * 在服务器应用后解析原生采集产物，普通配方沿用原模组的缓存刷新。
     *
     * @param manager 已应用覆盖结果的服务器配方管理器
     */
    public static void resolveHarvestOutputs(RecipeManager manager) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null || server.overworld() == null) return;
        for (var holder : manager.getRecipes()) {
            if (holder.value() instanceof CrystalCatalyzerRecipe recipe && recipe.outputSpec() instanceof CrystalHarvestOutput harvest)
                harvest.resolveHarvest(server.overworld());
        }
    }

    public static String defaults(ResourceLocation type) {
        var json = JsonParser.parseString(switch (type.getPath()) {
            case "lightning_transform", "lightning_simulation", "lightning_assembly" -> """
                    {"inputs":[{"ingredient":{"item":"minecraft:iron_ingot"},"count":1}],
                     "result":{"id":"minecraft:gold_ingot","count":1},"totalEnergy":1000,"lightningCost":1}
                    """;
            case "overload_processing", "firmament_conversion" -> """
                    {"inputs":[{"ingredient":{"item":"minecraft:iron_ingot"},"count":1}],
                     "results":[{"id":"minecraft:gold_ingot","count":1}],"totalEnergy":1000,"lightningCost":1,"processTime":100}
                    """;
            case "crystal_catalyzer" -> """
                    {"catalyst":{"item":"minecraft:amethyst_shard"},"catalystCount":1,
                     "output":{"id":"minecraft:amethyst_shard","count":1},"energyPerCycle":1000,"lightningCost":1,
                     "inputFluid":{"id":"minecraft:water","amount":1000},"mode":"crystal"}
                    """;
            case "lightning_strike" -> """
                    {"center_input":"minecraft:iron_block","center_output":"minecraft:gold_block",
                     "requirements":[{"offset":[1,0,0],"block":"minecraft:copper_block","consume":false}]}
                    """;
            default -> throw new IllegalArgumentException("Unsupported AE2 Lightning Tech editor: " + type);
        }).getAsJsonObject();
        json.addProperty("type", type.toString());
        return encode(compile(type, json.toString()), Platform.getFrozenRegistry());
    }
}
