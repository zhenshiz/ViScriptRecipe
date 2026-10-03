package com.viscript_recipe.compat.irons_spellbooks;

import com.viscript_recipe.compat.irons_spellbooks.data.IronAlchemistCauldronRecipeData;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.EmptyAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.FillAlchemistCauldronRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;
import java.util.Optional;

public final class IronSpellbooksRecipeFactory {
    private IronSpellbooksRecipeFactory() {
    }

    public static Recipe<?> compileFill(ResourceLocation recipeId, IronAlchemistCauldronRecipeData data) {
        var input = compileInput(data);
        var result = requireItem(data.getResult(), "Alchemist cauldron fill result cannot be empty");
        var fluid = requireFluid(data.getFluid(), "Alchemist cauldron fill fluid cannot be empty");
        return new FillAlchemistCauldronRecipe(recipeId, input, result, fluid, data.isMustFitAll(), sound(data.getSound(), SoundEvents.BOTTLE_EMPTY));
    }

    public static Recipe<?> compileEmpty(ResourceLocation recipeId, IronAlchemistCauldronRecipeData data) {
        var input = compileInput(data);
        var result = requireItem(data.getResult(), "Alchemist cauldron empty result cannot be empty");
        var fluid = requireFluid(data.getFluid(), "Alchemist cauldron empty fluid cannot be empty");
        return new EmptyAlchemistCauldronRecipe(recipeId, input, result, fluid, sound(data.getSound(), SoundEvents.BOTTLE_FILL));
    }

    public static Recipe<?> compileBrew(ResourceLocation recipeId, IronAlchemistCauldronRecipeData data) {
        var baseFluid = requireFluid(data.getBaseFluid(), "Alchemist cauldron brew base fluid cannot be empty");
        var input = compileInput(data);
        var results = data.getResultFluids() == null ? java.util.List.<FluidStack>of() : data.getResultFluids().stream()
                .filter(Objects::nonNull)
                .map(FluidStack::copy)
                .filter(stack -> !stack.isEmpty() && stack.getAmount() > 0)
                .toList();
        var byproduct = data.getByproduct() == null || data.getByproduct().isEmpty()
                ? Optional.<ItemStack>empty()
                : Optional.of(data.getByproduct().copy());
        if (results.isEmpty() && byproduct.isEmpty()) {
            throw new IllegalArgumentException("Alchemist cauldron brew must have a result fluid or byproduct");
        }
        return new BrewAlchemistCauldronRecipe(recipeId, baseFluid, input, results, byproduct);
    }

    private static Ingredient compileInput(IronAlchemistCauldronRecipeData data) {
        var input = data.getInput() == null ? Ingredient.EMPTY : data.getInput().compile();
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Alchemist cauldron input cannot be empty");
        }
        return input;
    }

    private static ItemStack requireItem(ItemStack stack, String message) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return stack.copy();
    }

    private static FluidStack requireFluid(FluidStack stack, String message) {
        if (stack == null || stack.isEmpty() || stack.getAmount() <= 0) {
            throw new IllegalArgumentException(message);
        }
        return stack.copy();
    }

    private static Holder<SoundEvent> sound(ResourceLocation id, SoundEvent fallback) {
        var event = id == null ? fallback : BuiltInRegistries.SOUND_EVENT.get(id);
        return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(event == null ? fallback : event);
    }
}
