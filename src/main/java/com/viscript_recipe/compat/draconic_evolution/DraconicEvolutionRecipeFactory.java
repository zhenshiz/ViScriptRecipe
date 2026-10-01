package com.viscript_recipe.compat.draconic_evolution;

import com.brandon3055.draconicevolution.api.crafting.FusionRecipe;
import com.brandon3055.draconicevolution.api.crafting.StackIngredient;
import com.viscript_recipe.compat.draconic_evolution.data.DraconicFusionRecipeData;
import com.viscript_recipe.compat.draconic_evolution.data.DraconicIngredientData;
import com.viscript_recipe.data.IngredientValueKind;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;

import java.util.ArrayList;
import java.util.Arrays;

public final class DraconicEvolutionRecipeFactory {
    private DraconicEvolutionRecipeFactory() {}

    public static FusionRecipe compile(DraconicFusionRecipeData data) {
        if (data.getResult() == null || data.getResult().isEmpty()) {
            throw new IllegalArgumentException("Fusion crafting output cannot be empty");
        }
        if (data.getTotalEnergy() <= 0 || data.getTechLevel() == null) {
            throw new IllegalArgumentException("Fusion crafting needs positive energy and a tech level");
        }
        var ingredients = new ArrayList<FusionRecipe.FusionIngredient>();
        for (var injector : data.getInjectors()) {
            if (injector == null || injector.getIngredient() == null || injector.getIngredient().isEmpty()) continue;
            ingredients.add(new FusionRecipe.FusionIngredient(compileIngredient(injector.getIngredient()), injector.isConsume()));
        }
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Fusion crafting needs at least one injector ingredient");
        }
        return new FusionRecipe(data.getResult().copy(), compileIngredient(data.getCatalyst()),
                data.getTotalEnergy(), data.getTechLevel(), ingredients);
    }

    public static Ingredient compileIngredient(DraconicIngredientData data) {
        if (data == null || data.isEmpty() || data.getCount() < 1) {
            throw new IllegalArgumentException("Fusion crafting ingredient and count cannot be empty");
        }
        var alternatives = data.getAlternatives().stream().filter(value -> !value.isEmpty()).toList();
        if (data.isStackIngredient() || data.getCount() > 1) {
            // DE's counted catalyst must stay a top-level StackIngredient: completeCraft checks its class.
            if (alternatives.size() == 1 && alternatives.getFirst().getKind() == IngredientValueKind.TAG) {
                var tag = TagKey.create(Registries.ITEM, alternatives.getFirst().getTag());
                return StackIngredient.of(data.getCount(), BuiltInRegistries.ITEM.getOrCreateTag(tag));
            }
            var items = new ArrayList<Holder<Item>>();
            for (var alternative : alternatives) {
                var compiled = alternative.compile();
                if (compiled.isCustom()) {
                    throw new IllegalArgumentException("Draconic stack ingredients support items and tags without component predicates");
                }
                for (var stack : compiled.getItems()) {
                    var holder = stack.getItemHolder();
                    if (!items.contains(holder)) items.add(holder);
                }
            }
            if (items.isEmpty()) throw new IllegalArgumentException("Draconic stack ingredient has no matching items");
            return StackIngredient.of(data.getCount(), HolderSet.direct(items));
        }
        var children = alternatives.stream().map(value -> value.compile()).toList();
        if (children.size() == 1) return children.getFirst();
        if (children.stream().anyMatch(Ingredient::isCustom)) {
            return CompoundIngredient.of(children.toArray(Ingredient[]::new));
        }
        return Ingredient.fromValues(children.stream().flatMap(value -> Arrays.stream(value.getValues())));
    }
}
