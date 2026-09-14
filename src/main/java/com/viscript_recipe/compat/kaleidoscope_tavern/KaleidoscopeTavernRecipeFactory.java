package com.viscript_recipe.compat.kaleidoscope_tavern;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.PressingTubRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.ShakerRecipe;
import com.viscript_recipe.compat.kaleidoscope_tavern.data.KaleidoscopeBarrelRecipeData;
import com.viscript_recipe.compat.kaleidoscope_tavern.data.KaleidoscopePressingTubRecipeData;
import com.viscript_recipe.compat.kaleidoscope_tavern.data.KaleidoscopeShakerRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Objects;

public final class KaleidoscopeTavernRecipeFactory {
    public static final int BARREL_MAX_INGREDIENTS = 4;
    public static final int SHAKER_MAX_INGREDIENTS = 3;
    public static final int DEFAULT_UNIT_TIME = 2400;
    public static final int DEFAULT_FLUID_AMOUNT = 125;
    private static final ResourceLocation DEFAULT_FLUID = ResourceLocation.withDefaultNamespace("water");

    private KaleidoscopeTavernRecipeFactory() {
    }

    public static Recipe<?> compileBarrel(KaleidoscopeBarrelRecipeData data) {
        var ingredients = NonNullList.withSize(BARREL_MAX_INGREDIENTS, Ingredient.EMPTY);
        var compiled = compileIngredients(data.getIngredients(), BARREL_MAX_INGREDIENTS);
        for (int i = 0; i < compiled.size(); i++) {
            ingredients.set(i, compiled.get(i));
        }
        return new BarrelRecipe(
                ingredients,
                requireFluid(data.getFluid(), "Kaleidoscope Tavern barrel fluid cannot be empty"),
                compileOptionalIngredient(data.getCarrier(), Ingredient.EMPTY),
                requireItem(data.getResult(), "Kaleidoscope Tavern barrel result cannot be empty"),
                Math.max(1, data.getUnitTime())
        );
    }

    public static Recipe<?> compilePressingTub(KaleidoscopePressingTubRecipeData data) {
        var ingredient = requireIngredient(data.getIngredient(), "Kaleidoscope Tavern pressing tub input cannot be empty");
        return new PressingTubRecipe(
                ingredient,
                requireFluid(data.getFluid(), "Kaleidoscope Tavern pressing tub fluid cannot be empty"),
                Math.max(1, data.getFluidAmount())
        );
    }

    public static Recipe<?> compileShaker(KaleidoscopeShakerRecipeData data) {
        var ingredients = NonNullList.withSize(SHAKER_MAX_INGREDIENTS, Ingredient.EMPTY);
        var compiled = compileIngredients(data.getIngredients(), SHAKER_MAX_INGREDIENTS);
        for (int i = 0; i < compiled.size(); i++) {
            ingredients.set(i, compiled.get(i));
        }
        return new ShakerRecipe(
                ingredients,
                requireItem(data.getResult(), "Kaleidoscope Tavern shaker result cannot be empty"),
                Int2ObjectMaps.emptyMap()
        );
    }

    /**需要踩踏多少次才能压榨出一桶流体*/
    public static int needPressCount(int fluidAmount) {
        if (fluidAmount <= 0) {
            return 1;
        }
        int count = 1000 / fluidAmount;
        if (count * fluidAmount < 1000) {
            count++;
        }
        return Math.max(1, count);
    }

    private static List<Ingredient> compileIngredients(List<RecipeIngredient> ingredients, int max) {
        var compiled = new java.util.ArrayList<Ingredient>();
        for (var ingredientData : ingredients == null ? List.<RecipeIngredient>of() : ingredients) {
            if (compiled.size() >= max) {
                break;
            }
            var ingredient = compileIngredient(ingredientData);
            if (!ingredient.isEmpty()) {
                compiled.add(ingredient);
            }
        }
        return compiled;
    }

    private static Ingredient requireIngredient(RecipeIngredient ingredient, String message) {
        var compiled = compileIngredient(ingredient);
        if (compiled.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return compiled;
    }

    private static Ingredient compileOptionalIngredient(RecipeIngredient ingredient, Ingredient fallback) {
        var compiled = compileIngredient(ingredient);
        return compiled.isEmpty() ? fallback : compiled;
    }

    private static Ingredient compileIngredient(RecipeIngredient ingredient) {
        return ingredient == null ? Ingredient.EMPTY : ingredient.compile();
    }

    private static ItemStack requireItem(ItemStack stack, String message) {
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            throw new IllegalArgumentException(message);
        }
        var copy = stack.copy();
        copy.setCount(Math.clamp(copy.getCount(), 1, 99));
        return copy;
    }

    private static Fluid requireFluid(ResourceLocation id, String message) {
        if (id == null) {
            id = DEFAULT_FLUID;
        }
        var fluid = BuiltInRegistries.FLUID.get(id);
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new IllegalArgumentException(message + ": " + id);
        }
        return fluid;
    }

    public static ResourceLocation fluidId(Fluid fluid) {
        return Objects.requireNonNullElse(BuiltInRegistries.FLUID.getKey(fluid), DEFAULT_FLUID);
    }
}
