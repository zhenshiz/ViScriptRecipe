package com.viscript_recipe.uitest;

import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.ServerContext;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.RecipeIngredient;
import io.netty.buffer.Unpooled;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

@LDLRegisterClient(name = "component_stack_ingredient", group = ViScriptRecipe.MOD_ID, registry = UIScenario.REGISTRY,
        environment = RegistrationEnvironment.DEV_ONLY)
public final class ComponentStackIngredientScenario implements UIScenario {
    @Override
    public void configure(ScenarioOptions options) {
        options.defaultTimeoutMs(30_000).tags("recipe", "ingredient", "components");
    }

    @Override
    public void define(ScenarioBuilder scenario) {
        scenario.server("preserve strict custom names through ingredient codecs", context -> {
                    var named = named(Items.DIAMOND, "VSR named ingredient");
                    var extraComponent = named.copy();
                    extraComponent.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(12));
                    verifyRoundTrips(context, RecipeIngredient.item(named).compile(), false, List.of(
                            new Candidate("exact custom name", named.copy(), true),
                            new Candidate("matching stack with a different count", named.copyWithCount(8), true),
                            new Candidate("missing custom name", new ItemStack(Items.DIAMOND), false),
                            new Candidate("different custom name", named(Items.DIAMOND, "Other name"), false),
                            new Candidate("same name on a different item", named(Items.EMERALD, "VSR named ingredient"), false),
                            new Candidate("additional component", extraComponent, false)));
                })
                .server("preserve explicit default-component removal through ingredient codecs", context -> {
                    var original = new ItemStack(Items.DIAMOND_SWORD);
                    context.check("fixture sword has a default damage component", original.has(DataComponents.DAMAGE));
                    var removed = original.copy();
                    removed.remove(DataComponents.DAMAGE);
                    var damaged = original.copy();
                    damaged.set(DataComponents.DAMAGE, 1);
                    var otherItem = new ItemStack(Items.IRON_SWORD);
                    otherItem.remove(DataComponents.DAMAGE);
                    verifyRoundTrips(context, RecipeIngredient.item(removed).compile(), false, List.of(
                            new Candidate("explicitly removed damage component", removed.copy(), true),
                            new Candidate("restored default damage component", original, false),
                            new Candidate("nonzero damage component", damaged, false),
                            new Candidate("removed component on a different item", otherItem, false)));
                })
                .server("preserve removal of an absent default component through ingredient codecs", context -> {
                    var ops = RegistryOps.create(JsonOps.INSTANCE, context.level().registryAccess());
                    var ingredient = Ingredient.CODEC.parse(ops, JsonParser.parseString("""
                            {"type":"viscript_recipe:component_stack","stack":{
                              "id":"minecraft:diamond","components":{"!minecraft:custom_model_data":{}}}}
                            """)).getOrThrow();
                    var withModel = new ItemStack(Items.DIAMOND);
                    withModel.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(12));
                    verifyRoundTrips(context, ingredient, false, List.of(
                            new Candidate("diamond without the removed component", new ItemStack(Items.DIAMOND), true),
                            new Candidate("diamond with the removed component restored", withModel, false)));
                })
                .server("keep plain potion item ingredients independent of potion type", context ->
                        verifyRoundTrips(context, RecipeIngredient.item(Items.POTION).compile(), true, List.of(
                                new Candidate("plain potion stack", new ItemStack(Items.POTION), true),
                                new Candidate("water bottle", PotionContents.createItemStack(Items.POTION, Potions.WATER), true),
                                new Candidate("awkward potion", PotionContents.createItemStack(Items.POTION, Potions.AWKWARD), true),
                                new Candidate("healing potion", PotionContents.createItemStack(Items.POTION, Potions.HEALING), true),
                                new Candidate("custom named potion", named(Items.POTION, "Any potion"), true),
                                new Candidate("splash potion", PotionContents.createItemStack(Items.SPLASH_POTION, Potions.WATER), false),
                                new Candidate("empty glass bottle", new ItemStack(Items.GLASS_BOTTLE), false))))
                .server("keep explicitly selected potion contents strict", context -> {
                    var water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
                    var namedWater = water.copy();
                    namedWater.set(DataComponents.CUSTOM_NAME, Component.literal("Named water"));
                    verifyRoundTrips(context, RecipeIngredient.item(water).compile(), false, List.of(
                            new Candidate("water bottle", water.copy(), true),
                            new Candidate("plain potion without selected contents", new ItemStack(Items.POTION), false),
                            new Candidate("awkward potion", PotionContents.createItemStack(Items.POTION, Potions.AWKWARD), false),
                            new Candidate("healing potion", PotionContents.createItemStack(Items.POTION, Potions.HEALING), false),
                            new Candidate("water with additional custom name", namedWater, false)));
                });
    }

    private static ItemStack named(Item item, String name) {
        var stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private static void verifyRoundTrips(ServerContext context, Ingredient original, boolean simple, List<Candidate> candidates) {
        verify(context, "compiled", original, simple, candidates);
        var ops = RegistryOps.create(JsonOps.INSTANCE, context.level().registryAccess());
        var json = Ingredient.CODEC.encodeStart(ops, original).getOrThrow();
        context.log("ingredient JSON: " + json);
        var decoded = Ingredient.CODEC.parse(ops, json).getOrThrow();
        verify(context, "JSON round trip", decoded, simple, candidates);

        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), context.level().registryAccess());
        try {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, original);
            var streamed = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            context.check("network ingredient consumed its complete payload", !buffer.isReadable());
            verify(context, "network round trip", streamed, simple, candidates);
        } finally {
            buffer.release();
        }
    }

    private static void verify(ServerContext context, String stage, Ingredient ingredient, boolean simple, List<Candidate> candidates) {
        context.check(stage + " matching mode", ingredient.isSimple() == simple, simple, ingredient.isSimple());
        context.check(stage + " rejects an empty stack", !ingredient.test(ItemStack.EMPTY));
        for (var candidate : candidates) {
            boolean actual = ingredient.test(candidate.stack());
            context.check(stage + ": " + candidate.name(), actual == candidate.accepted(), candidate.accepted(), actual);
        }
    }

    private record Candidate(String name, ItemStack stack, boolean accepted) { }
}
