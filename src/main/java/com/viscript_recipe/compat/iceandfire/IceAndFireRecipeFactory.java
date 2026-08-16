package com.viscript_recipe.compat.iceandfire;

import com.iafenvoy.iceandfire.recipe.DragonForgeRecipe;
import com.viscript_recipe.compat.iceandfire.data.DragonForgeRecipeData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Set;

public final class IceAndFireRecipeFactory {
    private static final Set<String> DRAGON_TYPES = Set.of("fire", "ice", "lightning");

    private IceAndFireRecipeFactory() {
    }

    public static Recipe<?> compileDragonForge(DragonForgeRecipeData data) {
        var input = data.getInput() == null ? Ingredient.EMPTY : data.getInput().compile();
        var blood = data.getBlood() == null ? Ingredient.EMPTY : data.getBlood().compile();
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Dragon Forge input cannot be empty");
        }
        if (blood.isEmpty()) {
            throw new IllegalArgumentException("Dragon Forge blood/material cannot be empty");
        }
        var result = requireItem(data.getResult(), "Dragon Forge result cannot be empty");
        return new DragonForgeRecipe(input, blood, result, normalizeDragonType(data.getDragonType()), Math.max(1, data.getCookTime()));
    }

    public static String normalizeDragonType(String dragonType) {
        return dragonType != null && DRAGON_TYPES.contains(dragonType) ? dragonType : "fire";
    }

    private static ItemStack requireItem(ItemStack stack, String message) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return stack.copy();
    }
}
