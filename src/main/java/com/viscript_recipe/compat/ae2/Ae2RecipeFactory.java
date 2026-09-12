package com.viscript_recipe.compat.ae2;

import appeng.recipes.entropy.EntropyRecipe;
import appeng.recipes.entropy.EntropyRecipeBuilder;
import appeng.recipes.entropy.PropertyValueMatcher;
import appeng.recipes.handlers.ChargerRecipe;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import appeng.recipes.transform.TransformCircumstance;
import appeng.recipes.transform.TransformRecipe;
import com.viscript_recipe.compat.ae2.data.*;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.StateDefinition;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Validates editor data and builds native AE2 recipes using public constructors and builders. */
public final class Ae2RecipeFactory {
    private Ae2RecipeFactory() {}

    public static ChargerRecipe compile(Ae2ChargerRecipeData data) {
        return new ChargerRecipe(required(data.getInput()), result(data.getResult()));
    }

    public static InscriberRecipe compile(Ae2InscriberRecipeData data) {
        return new InscriberRecipe(required(data.getMiddle()), result(data.getResult()),
                data.getTop().compile(), data.getBottom().compile(),
                data.isConsumePresses() ? InscriberProcessType.PRESS : InscriberProcessType.INSCRIBE);
    }

    public static TransformRecipe compile(Ae2TransformRecipeData data) {
        var inputs = NonNullList.<Ingredient>create();
        for (var input : data.getInputs()) {
            if (!input.isEmpty()) inputs.add(required(input));
        }
        if (inputs.isEmpty()) throw new IllegalArgumentException("AE2 transformation requires an ingredient");
        return new TransformRecipe(inputs, result(data.getResult()), data.isExplosion()
                ? TransformCircumstance.explosion()
                : TransformCircumstance.fluid(TagKey.create(Registries.FLUID, data.getFluidTag())));
    }

    public static EntropyRecipe compile(Ae2EntropyRecipeData data) {
        var builder = data.isHeat() ? EntropyRecipeBuilder.heat() : EntropyRecipeBuilder.cool();
        if (data.isInputBlockEnabled()) {
            var block = registryValue(BuiltInRegistries.BLOCK, data.getInputBlock());
            builder.setInputBlock(block).setBlockStateMatchers(matchers(data.getInputBlockProperties(), block.getStateDefinition()));
        }
        if (data.isInputFluidEnabled()) {
            var fluid = registryValue(BuiltInRegistries.FLUID, data.getInputFluid());
            builder.setInputFluid(fluid).setFluidStateMatchers(matchers(data.getInputFluidProperties(), fluid.getStateDefinition()));
        }
        if (data.isOutputBlockEnabled()) {
            var block = registryValue(BuiltInRegistries.BLOCK, data.getOutputBlock());
            builder.setOutputBlock(block).setOutputBlockKeep(data.isKeepBlockProperties())
                    .setBlockStateAppliers(appliers(data.getOutputBlockProperties(), block.getStateDefinition()));
        }
        if (data.isOutputFluidEnabled()) {
            var fluid = registryValue(BuiltInRegistries.FLUID, data.getOutputFluid());
            builder.setOutputFluid(fluid).setOutputFluidKeep(data.isKeepFluidProperties())
                    .setFluidStateAppliers(appliers(data.getOutputFluidProperties(), fluid.getStateDefinition()));
        }
        var drops = data.getDrops().stream().filter(stack -> !stack.isEmpty()).map(Ae2RecipeFactory::result).toList();
        if (!drops.isEmpty()) builder.setDrops(drops);
        return builder.build();
    }

    private static <T> T registryValue(Registry<T> registry, ResourceLocation id) {
        return registry.getOptional(id).orElseThrow(() -> new IllegalArgumentException("Unknown AE2 recipe value: " + id));
    }

    private static Ingredient required(Ae2IngredientData input) {
        var ingredient = input.compile();
        if (ingredient.isEmpty()) throw new IllegalArgumentException("AE2 recipe input cannot be empty");
        return ingredient;
    }

    private static ItemStack result(ItemStack stack) {
        if (stack == null || stack.isEmpty()) throw new IllegalArgumentException("AE2 recipe output cannot be empty");
        return stack.copy();
    }

    private static Map<String, PropertyValueMatcher> matchers(List<Ae2StatePropertyData> rows, StateDefinition<?, ?> state) {
        var result = new LinkedHashMap<String, PropertyValueMatcher>();
        for (var row : rows) {
            var property = state.getProperty(row.getName());
            if (property == null) throw new IllegalArgumentException("Unknown state property: " + row.getName());
            PropertyValueMatcher matcher = switch (row.getMode()) {
                case SINGLE -> new PropertyValueMatcher.SingleValue(row.getValue());
                case MULTIPLE -> new PropertyValueMatcher.MultiValue(List.copyOf(row.getValues()));
                case RANGE -> new PropertyValueMatcher.Range(row.getMin(), row.getMax());
            };
            matcher.validate(property);
            if (result.put(row.getName(), matcher) != null) throw new IllegalArgumentException("Duplicate state property: " + row.getName());
        }
        return result;
    }

    private static Map<String, String> appliers(List<Ae2StatePropertyData> rows, StateDefinition<?, ?> state) {
        var result = new LinkedHashMap<String, String>();
        for (var row : rows) {
            var property = state.getProperty(row.getName());
            if (property == null || property.getValue(row.getValue()).isEmpty()) {
                throw new IllegalArgumentException("Invalid output state property: " + row.getName() + "=" + row.getValue());
            }
            if (result.put(row.getName(), row.getValue()) != null) throw new IllegalArgumentException("Duplicate state property: " + row.getName());
        }
        return result;
    }
}
