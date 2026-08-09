package com.viscript_recipe.compat.confluence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib2.Platform;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.confluence.*;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.confluence.mod.common.init.ModRecipes;

import java.util.List;
import java.util.Objects;

/** Converts editor data with the same native Codec used by Confluence's data packs. */
public final class ConfluenceRecipeFactory {
    private static final char[] PATTERN_SYMBOLS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    private ConfluenceRecipeFactory() {
    }

    public static Recipe<?> compile(ResourceLocation type, ConfluenceRecipeData data) {
        Objects.requireNonNull(data, "Confluence recipe data");
        var json = new JsonObject();
        if (ConfluenceRecipeEditorTypes.ITEM_TRANSMUTATION.equals(type)) {
            json.add("source", encodeIngredient(data.ingredient(0).getIngredient(), 1));
            var targets = new JsonArray();
            safeList(data.getTargets()).stream().filter(stack -> stack != null && !stack.isEmpty() && !stack.is(Items.AIR))
                    .limit(ConfluenceRecipeData.MAX_TRANSMUTATION_RESULTS)
                    .forEach(stack -> targets.add(encodeItem(stack)));
            json.add("target", targets);
            json.addProperty("shrink", Math.max(1, data.getShrink()));
            var phase = data.getGamePhase() == null ? ConfluenceGamePhase.BEFORE_SKELETRON : data.getGamePhase();
            json.addProperty("game_phase", phase.getSerializedName());
            return decode(ModRecipes.ITEM_TRANSMUTATION_SERIALIZER.get(), json);
        }
        json.add("result", encodeItem(requireResult(data)));
        if (ConfluenceRecipeEditorTypes.ALCHEMY_TABLE.equals(type)) {
            json.add("base", encodeIngredient(data.ingredient(0).getIngredient(), 1));
            json.add("ingredients", encodeIngredients(data, 1, 6));
        } else if (ConfluenceRecipeEditorTypes.FLETCHING_TABLE.equals(type)) {
            json.add("tail", encodeIngredient(data.ingredient(0).getIngredient(), 1));
            json.add("body", encodeIngredient(data.ingredient(1).getIngredient(), 1));
            json.add("head", encodeIngredient(data.ingredient(2).getIngredient(), 1));
        } else if (ConfluenceRecipeEditorTypes.COOKING_POT.equals(type)) {
            json.add("ingredients", encodeIngredients(data, 0, 4));
            json.add("container", encodeIngredient(data.getContainer(), 1));
            json.add("heat_source", encodeHeatSource(data.getHeatSource()));
            json.addProperty("cookingtime", Math.max(0, data.getCookingTime()));
        } else if (ConfluenceRecipeEditorTypes.HELLFORGE.equals(type)
                || ConfluenceRecipeEditorTypes.HARDMODE_FORGE.equals(type)) {
            json.add("ingredients", encodeIngredients(data, 0, 4));
            json.addProperty("experience", data.getExperience());
            json.addProperty("cookingtime", Math.max(0, data.getCookingTime()));
            json.addProperty("requires_fuel", data.isRequiresFuel());
        } else if (ConfluenceRecipeEditorTypes.HEAVY_WORK_BENCH.equals(type)) {
            encodeEither(json, data);
            json.add("environment", encodeEnvironment(data.getEnvironment()));
        } else if (ConfluenceRecipeEditorTypes.SOLIDIFIER.equals(type)) {
            encodeShaped(json, data);
        } else if (ConfluenceRecipeEditorTypes.SAWMILL.equals(type)
                || ConfluenceRecipeEditorTypes.HARDMODE_ANVIL.equals(type)
                || ConfluenceRecipeEditorTypes.LOOM.equals(type)) {
            encodeEither(json, data);
        } else if (ConfluenceRecipeEditorTypes.SKY_MILL.equals(type)
                || ConfluenceRecipeEditorTypes.CRYSTAL_BALL.equals(type)) {
            json.add("ingredients", encodeIngredients(data, 0, ConfluenceRecipeEditorTypes.maxInputs(type)));
            json.add("environment", encodeEnvironment(data.getEnvironment()));
        } else if (ConfluenceRecipeEditorTypes.ALTAR.equals(type)) {
            json.add("ingredients", encodeIngredients(data, 0, 5));
        } else if (ConfluenceRecipeEditorTypes.DYE_VAT.equals(type)) {
            json.add("ingredients", encodeIngredients(data, 0, 4));
        } else {
            throw new IllegalArgumentException("Unsupported Confluence recipe type: " + type);
        }
        return decode(serializer(type), json);
    }

    private static JsonArray encodeIngredients(ConfluenceRecipeData data, int offset, int max) {
        var result = new JsonArray();
        for (int index = 0; index < max; index++) {
            var value = data.ingredient(offset + index);
            var ingredient = value == null ? null : value.getIngredient();
            if (ingredient != null && !ingredient.isEmpty()) {
                result.add(encodeIngredient(ingredient, value.getCount()));
            }
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Confluence recipe must contain at least one ingredient");
        }
        return result;
    }

    private static void encodeEither(JsonObject json, ConfluenceRecipeData data) {
        if (data.getCraftingMode() == ConfluenceCraftingMode.SHAPELESS) {
            json.add("ingredients", encodeIngredients(data, 0, ConfluenceRecipeData.MAX_INPUTS));
        } else {
            encodeShaped(json, data);
        }
    }

    private static void encodeShaped(JsonObject json, ConfluenceRecipeData data) {
        var width = Math.clamp(data.getWidth(), 1, 4);
        var height = Math.clamp(data.getHeight(), 1, 4);
        var key = new JsonObject();
        var pattern = new JsonArray();
        var symbolIndex = 0;
        for (int row = 0; row < height; row++) {
            var line = new StringBuilder(width);
            for (int col = 0; col < width; col++) {
                var value = data.ingredient(row * 4 + col);
                if (value == null || value.getIngredient() == null || value.getIngredient().isEmpty()) {
                    line.append(' ');
                    continue;
                }
                var symbol = PATTERN_SYMBOLS[symbolIndex++];
                line.append(symbol);
                key.add(Character.toString(symbol), encodeIngredient(value.getIngredient(), value.getCount()));
            }
            pattern.add(line.toString());
        }
        if (key.size() == 0) {
            throw new IllegalArgumentException("Confluence shaped recipe must contain at least one ingredient");
        }
        json.add("pattern", pattern);
        json.add("key", key);
    }

    private static JsonObject encodeEnvironment(ConfluenceEnvironmentData data) {
        var environment = new JsonObject();
        if (data == null) {
            return environment;
        }
        putHolderSet(environment, "biome", data.getBiomes(), true);
        var search = new JsonObject();
        putHolderSet(search, "blocks", data.getBlocks(), true);
        putHolderSet(search, "fluids", data.getFluids(), true);
        var predicates = new JsonArray();
        if (data.getStatePredicates() != null) {
            for (var predicate : data.getStatePredicates()) {
                if (hasProperties(predicate)) {
                    predicates.add(statePredicateJson(predicate));
                }
            }
        }
        if (!search.entrySet().isEmpty() || !predicates.isEmpty()) {
            search.addProperty("inflate", Math.max(1, data.getInflate()));
            if (!predicates.isEmpty()) search.add("state_predicates", predicates);
            environment.add("block", search);
        }
        if (data.isGraveyard()) environment.addProperty("graveyard", true);
        return environment;
    }

    private static JsonObject encodeHeatSource(ConfluenceHeatSourceData data) {
        var heat = new JsonObject();
        if (data == null) return heat;
        putHolderSet(heat, "blocks", data.getBlocks(), false);
        if (data.isHasState() && hasProperties(data.getState())) heat.add("state", statePredicateJson(data.getState()));
        if (data.isHasNbt()) heat.add("nbt", encodeNbt(data.getNbt()));
        return heat;
    }

    private static com.google.gson.JsonElement encodeNbt(String text) {
        var source = text == null || text.isBlank() ? "{}" : text.trim();
        try {
            CompoundTag tag;
            try {
                tag = TagParser.parseTag(source);
            } catch (Exception snbtFailure) {
                var json = com.google.gson.JsonParser.parseString(source);
                tag = TagParser.parseTag(json.toString());
            }
            return NbtPredicate.CODEC.encodeStart(JsonOps.INSTANCE, new NbtPredicate(tag))
                    .getOrThrow(error -> new IllegalArgumentException("Unable to encode heat-source NBT: " + error));
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid cooking-pot heat-source SNBT", exception);
        }
    }

    private static JsonObject statePredicateJson(ConfluenceStatePredicateData data) {
        var json = new JsonObject();
        if (data == null || data.getProperties() == null) return json;
        for (var property : data.getProperties()) {
            if (property == null || property.getName() == null || property.getName().isBlank()) continue;
            if (property.isRanged()) {
                var range = new JsonObject();
                if (property.getMin() != null && !property.getMin().isBlank()) range.addProperty("min", property.getMin());
                if (property.getMax() != null && !property.getMax().isBlank()) range.addProperty("max", property.getMax());
                json.add(property.getName(), range);
            } else {
                json.addProperty(property.getName(), property.getValue() == null ? "" : property.getValue());
            }
        }
        return json;
    }

    private static void putHolderSet(JsonObject json, String name, ConfluenceHolderSetData data, boolean hashTag) {
        if (data == null || data.getKind() == null || data.getKind() == ConfluenceHolderSetKind.NONE) return;
        if (data.getKind() == ConfluenceHolderSetKind.TAG) {
            if (data.getTag() == null) throw new IllegalArgumentException(name + " tag cannot be empty");
            json.addProperty(name, (hashTag ? "#" : "") + data.getTag());
            return;
        }
        var values = new JsonArray();
        if (data.getValues() != null) data.getValues().stream().filter(Objects::nonNull).forEach(id -> values.add(id.toString()));
        if (!values.isEmpty()) json.add(name, values);
    }

    private static com.google.gson.JsonElement encodeIngredient(RecipeIngredient data, int count) {
        var ingredient = data == null ? Ingredient.EMPTY : data.compile();
        if (ingredient.isEmpty()) throw new IllegalArgumentException("Confluence ingredient cannot be empty");
        var ops = RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry());
        var encoded = Ingredient.CODEC.encodeStart(ops, ingredient)
                .getOrThrow(error -> new IllegalArgumentException("Unable to encode ingredient: " + error));
        if (Math.max(1, count) == 1) return encoded;
        var amount = new JsonObject();
        amount.addProperty("type", "confluence_magic_lib:amount_ingredient");
        amount.addProperty("count", Math.max(1, count));
        amount.add("ingredient", encoded);
        return amount;
    }

    private static com.google.gson.JsonElement encodeItem(ItemStack stack) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry());
        return ItemStack.STRICT_CODEC.encodeStart(ops, stack.copy())
                .getOrThrow(error -> new IllegalArgumentException("Unable to encode result: " + error));
    }

    @SuppressWarnings("unchecked")
    private static <R extends Recipe<?>> R decode(RecipeSerializer<?> serializer, JsonObject json) {
        var ops = RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry());
        return (R) serializer.codec().codec().parse(ops, json)
                .getOrThrow(error -> new IllegalArgumentException("Invalid Confluence recipe: " + error));
    }

    private static RecipeSerializer<?> serializer(ResourceLocation type) {
        if (ConfluenceRecipeEditorTypes.SKY_MILL.equals(type)) return ModRecipes.SKY_MILL_SERIALIZER.get();
        if (ConfluenceRecipeEditorTypes.ALTAR.equals(type)) return ModRecipes.ALTAR_SERIALIZER.get();
        if (ConfluenceRecipeEditorTypes.HELLFORGE.equals(type)) return ModRecipes.HELLFORGE_SERIALIZER.get();
        if (ConfluenceRecipeEditorTypes.HEAVY_WORK_BENCH.equals(type)) return ModRecipes.HEAVY_WORK_BENCH_SERIALIZER.get();
        if (ConfluenceRecipeEditorTypes.ALCHEMY_TABLE.equals(type)) return ModRecipes.ALCHEMY_TABLE_SERIALIZER.get();
        if (ConfluenceRecipeEditorTypes.FLETCHING_TABLE.equals(type)) return ModRecipes.FLETCHING_TABLE_SERIALIZER.get();
        if (ConfluenceRecipeEditorTypes.COOKING_POT.equals(type)) return ModRecipes.COOKING_POT_SERIALIZER.get();
        if (ConfluenceRecipeEditorTypes.SAWMILL.equals(type)) return ModRecipes.SAWMILL_SERIALIZER.get();
        if (ConfluenceRecipeEditorTypes.SOLIDIFIER.equals(type)) return ModRecipes.SOLIDIFIER_SERIALIZER.get();
        if (ConfluenceRecipeEditorTypes.HARDMODE_ANVIL.equals(type)) return ModRecipes.HARDMODE_ANVIL_SERIALIZER.get();
        if (ConfluenceRecipeEditorTypes.HARDMODE_FORGE.equals(type)) return ModRecipes.HARDMODE_FORGE_SERIALIZER.get();
        if (ConfluenceRecipeEditorTypes.LOOM.equals(type)) return ModRecipes.LOOM_SERIALIZER.get();
        if (ConfluenceRecipeEditorTypes.DYE_VAT.equals(type)) return ModRecipes.DYE_VAT_SERIALIZER.get();
        if (ConfluenceRecipeEditorTypes.CRYSTAL_BALL.equals(type)) return ModRecipes.CRYSTAL_BALL_SERIALIZER.get();
        throw new IllegalArgumentException("Unknown Confluence serializer: " + type);
    }

    private static ItemStack requireResult(ConfluenceRecipeData data) {
        var stack = data.getResult();
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) throw new IllegalArgumentException("Confluence result cannot be empty");
        return stack;
    }

    private static boolean hasProperties(ConfluenceStatePredicateData data) {
        return data != null && data.getProperties() != null && data.getProperties().stream()
                .anyMatch(p -> p != null && p.getName() != null && !p.getName().isBlank());
    }

    private static <T> List<T> safeList(List<T> values) { return values == null ? List.of() : values; }

}
