package com.viscript_recipe.compat.cataclysm;

import com.github.L_Ender.cataclysm.crafting.AltarOfAmethystRecipe;
import com.github.L_Ender.cataclysm.crafting.WeaponfusionRecipe;
import com.viscript_recipe.compat.cataclysm.data.CataclysmAmethystBlessRecipeData;
import com.viscript_recipe.compat.cataclysm.data.CataclysmWeaponFusionRecipeData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Creates Cataclysm's native recipe objects from editor data.
 */
public final class CataclysmRecipeFactory {
    private CataclysmRecipeFactory() {
    }

    /**
     * Compiles a mechanical fusion anvil recipe. Returning Cataclysm's native class is important because its
     * assembly logic transfers the base item's data components to the fused result.
     *
     * @param data editor recipe data
     * @return native weapon fusion recipe
     */
    public static Recipe<?> compileWeaponFusion(CataclysmWeaponFusionRecipeData data) {
        var base = requireIngredient(data.getBase(), "Weapon fusion base ingredient cannot be empty");
        var addition = requireIngredient(data.getAddition(), "Weapon fusion addition ingredient cannot be empty");
        var result = requireItem(data.getResult(), "Weapon fusion result cannot be empty");
        return new WeaponfusionRecipe(base, addition, result);
    }

    /**
     * Compiles an Altar of Amethyst blessing recipe.
     *
     * @param data editor recipe data
     * @return native amethyst blessing recipe
     */
    public static Recipe<?> compileAmethystBless(CataclysmAmethystBlessRecipeData data) {
        var ingredient = requireIngredient(data.getIngredient(), "Amethyst blessing ingredient cannot be empty");
        var result = requireItem(data.getResult(), "Amethyst blessing result cannot be empty");
        return new AltarOfAmethystRecipe(ingredient, result, Math.max(1, data.getTime()));
    }

    private static Ingredient requireIngredient(com.viscript_recipe.data.RecipeIngredient data, String message) {
        var ingredient = data == null ? Ingredient.EMPTY : data.compile();
        if (ingredient.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return ingredient;
    }

    private static ItemStack requireItem(ItemStack stack, String message) {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return stack.copy();
    }
}
