package com.viscript_recipe.compat.eidolon;

import alexthw.eidolon_repraised.recipe.*;
import alexthw.eidolon_repraised.registries.RitualRegistry;
import com.lowdragmc.lowdraglib2.Platform;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.RegistryOps;
import com.viscript_recipe.compat.eidolon.data.*;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EidolonRecipeFactory {
    private EidolonRecipeFactory() {}

    public static CrucibleRecipe crucible(EidolonCrucibleRecipeData data) {
        if (data.getSteps().isEmpty()) throw new IllegalArgumentException("Crucible recipe needs at least one step");
        var steps = new ArrayList<CrucibleRecipe.Step>();
        for (var step : data.getSteps()) {
            if (step.getStirs() < 0) throw new IllegalArgumentException("Crucible stirs cannot be negative");
            steps.add(new CrucibleRecipe.Step(step.getStirs(), ingredients(step.getItems())));
        }
        return new CrucibleRecipe(steps, result(data.getResult()));
    }

    public static WorktableRecipe worktable(EidolonWorktableRecipeData data) {
        if (data.getWidth() < 1 || data.getWidth() > 3 || data.getHeight() < 1 || data.getHeight() > 3
                || data.getCore().size() != 9 || data.getReagents().size() != 4) {
            throw new IllegalArgumentException("Worktable needs a 1-3 by 1-3 core grid and four reagents");
        }
        var core = NonNullList.withSize(data.getWidth() * data.getHeight(), Ingredient.EMPTY);
        var outer = NonNullList.withSize(4, Ingredient.EMPTY);
        var keys = new LinkedHashMap<Character, Ingredient>();
        var rows = new ArrayList<String>();
        for (int y = 0; y < data.getHeight(); y++) {
            var row = new StringBuilder();
            for (int x = 0; x < data.getWidth(); x++) {
                int index = y * 3 + x;
                var ingredient = data.getCore().get(index).compile();
                core.set(y * data.getWidth() + x, ingredient);
                row.append(symbol(keys, ingredient, (char) ('A' + index)));
            }
            rows.add(row.toString());
        }
        if (core.stream().allMatch(Ingredient::isEmpty)) throw new IllegalArgumentException("Worktable core cannot be empty");
        var reagentRow = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            var ingredient = data.getReagents().get(i).compile();
            outer.set(i, ingredient);
            reagentRow.append(symbol(keys, ingredient, (char) ('J' + i)));
        }
        if (outer.stream().allMatch(Ingredient::isEmpty)) throw new IllegalArgumentException("Worktable reagents cannot be empty");
        // 两个原生 Codec 都写入 key 字段，必须传入同一份完整映射。
        var corePattern = WorktableRecipe.Serializer.PATTERN_CORE_CODEC.codec().parse(
                RegistryOps.create(JsonOps.INSTANCE,
                        Platform.getFrozenRegistry()), patternJson(keys, rows, "pattern")).getOrThrow();
        var outerPattern = WorktableRecipe.Serializer.PATTERN_OUTER_CODEC.codec().parse(
                RegistryOps.create(JsonOps.INSTANCE,
                        Platform.getFrozenRegistry()), patternJson(keys, List.of(reagentRow.toString()), "reagents")).getOrThrow();
        return new WorktableRecipe(corePattern, outerPattern, result(data.getResult()));
    }

    private static JsonObject patternJson(Map<Character, Ingredient> keys, List<String> rows, String name) {
        var json = new JsonObject();
        var keyJson = new JsonObject();
        var ops = RegistryOps.create(JsonOps.INSTANCE,
                Platform.getFrozenRegistry());
        keys.forEach((symbol, ingredient) -> keyJson.add(String.valueOf(symbol), Ingredient.CODEC.encodeStart(ops, ingredient).getOrThrow()));
        var rowJson = new JsonArray();
        rows.forEach(rowJson::add);
        json.add("key", keyJson); json.add(name, rowJson);
        return json;
    }

    private static char symbol(Map<Character, Ingredient> keys, Ingredient ingredient, char symbol) {
        if (ingredient.isEmpty()) return ' ';
        keys.put(symbol, ingredient);
        return symbol;
    }

    public static DyeRecipe dye(EidolonDyeRecipeData data) {
        var inputs = ingredients(data.getInputs());
        if (inputs.isEmpty() || inputs.size() > 9) throw new IllegalArgumentException("Dye recipe needs 1-9 ingredients");
        var list = NonNullList.<Ingredient>create(); list.addAll(inputs);
        // 原生 DyeRecipe 会保留输入的数据组件，并在 assemble() 中应用染料。
        return new DyeRecipe(data.getGroup(), data.getCategory(), result(data.getResult()), list);
    }

    public static Recipe<?> ritual(EidolonRitualRecipeData data, ResourceLocation typeId) {
        if (!Float.isFinite(data.getHealthRequirement()) || data.getHealthRequirement() < 0) {
            throw new IllegalArgumentException("Ritual health requirement must be finite and nonnegative");
        }
        var reagent = data.getReagent().compile();
        var pedestals = ingredients(data.getPedestals());
        var foci = ingredients(data.getFoci());
        float health = data.getHealthRequirement();
        if (typeId.equals(EidolonRecipeEditorTypes.GENERIC_RITUAL)) {
            if (data.getRitual() == null || RitualRegistry.find(data.getRitual()) == null) {
                throw new IllegalArgumentException("Unknown Eidolon ritual: " + data.getRitual());
            }
            return new GenericRitualRecipe(data.getRitual(), reagent, pedestals, foci, ingredients(data.getInvariants()), health);
        }
        if (typeId.equals(EidolonRecipeEditorTypes.ITEM_RITUAL)) {
            return new ItemRitualRecipe(reagent, pedestals, foci, result(data.getResult()),
                    data.getSymbol(), data.getColor(), data.isKeepReagentComponents(), health);
        }
        if (typeId.equals(EidolonRecipeEditorTypes.SUMMON_RITUAL)) {
            if (data.getEntity() == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(data.getEntity()) || data.getSummonCount() < 1) {
                throw new IllegalArgumentException("Summoning needs a registered entity and a positive count");
            }
            return new SummonRitualRecipe(data.getEntity(), reagent, pedestals, foci, data.getSummonCount(), health);
        }
        if (typeId.equals(EidolonRecipeEditorTypes.LOCATION_RITUAL)) {
            if (data.getStructureTag() == null) throw new IllegalArgumentException("Structure tag cannot be empty");
            return new LocationRitualRecipe(data.getStructureTag(), reagent, pedestals, foci, health);
        }
        if (typeId.equals(EidolonRecipeEditorTypes.COMMAND_RITUAL)) {
            if (data.getCommands().isEmpty() || data.getCommands().stream().anyMatch(String::isBlank)) {
                throw new IllegalArgumentException("Command ritual needs nonempty commands");
            }
            return new CommandRitualRecipe(new ArrayList<>(data.getCommands()), reagent, pedestals, foci,
                    data.getSymbol(), data.getColor(), health);
        }
        throw new IllegalArgumentException("Unsupported Eidolon ritual type: " + typeId);
    }

    public static List<Ingredient> ingredients(List<EidolonIngredientData> data) {
        return data.stream().filter(input -> !input.isEmpty()).map(EidolonIngredientData::compile).toList();
    }

    private static ItemStack result(ItemStack stack) {
        if (stack == null || stack.isEmpty()) throw new IllegalArgumentException("Eidolon recipe output cannot be empty");
        return stack.copy();
    }
}
