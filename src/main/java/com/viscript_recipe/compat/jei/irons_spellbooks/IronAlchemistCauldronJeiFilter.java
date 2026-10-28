package com.viscript_recipe.compat.jei.irons_spellbooks;

import io.redspace.ironsspellbooks.jei.AlchemistCauldronJeiRecipe;
import io.redspace.ironsspellbooks.jei.AlchemistCauldronRecipeCategory;
import io.redspace.ironsspellbooks.jei.AlchemistCauldronRecipeMaker;
import io.redspace.ironsspellbooks.recipe_types.alchemist_cauldron.BrewAlchemistCauldronRecipe;
import io.redspace.ironsspellbooks.registries.RecipeRegistry;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public final class IronAlchemistCauldronJeiFilter {
    private IronAlchemistCauldronJeiFilter() {}

    public static void apply(IJeiRuntime runtime, boolean showcaseOnly) {
        if (showcaseOnly) applyAlchemistShowcaseFilter(runtime.getRecipeManager());
        else restoreAlchemistRecipes(runtime.getRecipeManager());
    }

    private static void applyAlchemistShowcaseFilter(IRecipeManager recipeManager) {
        var type = AlchemistCauldronRecipeCategory.ALCHEMIST_CAULDRON_RECIPE_TYPE;
        var allowed = uniqueAlchemistRecipes(currentAlchemistBrewRecipes());
        var existing = allJeiRecipes(recipeManager, type);
        var hidden = new ArrayList<>(existing.stream()
                .filter(recipe -> !matchesAny(allowed, recipe))
                .toList());
        hidden.addAll(duplicateAlchemistRecipes(existing));
        var visible = uniqueAlchemistRecipes(existing.stream()
                .filter(recipe -> matchesAny(allowed, recipe))
                .toList());
        var missing = allowed.stream()
                .filter(recipe -> existing.stream().noneMatch(existingRecipe -> sameAlchemistRecipe(recipe, existingRecipe)))
                .toList();

        if (!hidden.isEmpty()) {
            recipeManager.hideRecipes(type, hidden);
        }
        if (!visible.isEmpty()) {
            recipeManager.unhideRecipes(type, visible);
        }
        if (!missing.isEmpty()) {
            recipeManager.addRecipes(type, missing);
        }
        if (allowed.isEmpty()) {
            recipeManager.hideRecipeCategory(type);
        } else {
            recipeManager.unhideRecipeCategory(type);
        }
        var advancedRecipes = new ArrayList<AlchemistCauldronJeiRecipe>(visible.size() + missing.size());
        advancedRecipes.addAll(visible);
        advancedRecipes.addAll(missing);
        AlchemistCauldronRecipeMaker.recipes = List.copyOf(advancedRecipes);
    }

    private static void restoreAlchemistRecipes(IRecipeManager recipeManager) {
        var type = AlchemistCauldronRecipeCategory.ALCHEMIST_CAULDRON_RECIPE_TYPE;
        var allRecipes = allJeiRecipes(recipeManager, type);
        var recipes = uniqueAlchemistRecipes(allRecipes);
        var duplicates = duplicateAlchemistRecipes(allRecipes);
        if (!duplicates.isEmpty()) {
            recipeManager.hideRecipes(type, duplicates);
        }
        if (!recipes.isEmpty()) {
            recipeManager.unhideRecipes(type, recipes);
        }
        recipeManager.unhideRecipeCategory(type);
        AlchemistCauldronRecipeMaker.recipes = List.copyOf(recipes);
    }

    private static List<AlchemistCauldronJeiRecipe> currentAlchemistBrewRecipes() {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        return level.getRecipeManager()
                .getAllRecipesFor(RecipeRegistry.ALCHEMIST_CAULDRON_BREW_TYPE.get())
                .stream()
                .map(IronAlchemistCauldronJeiFilter::toJeiRecipe)
                .toList();
    }

    private static AlchemistCauldronJeiRecipe toJeiRecipe(BrewAlchemistCauldronRecipe recipe) {
        return new AlchemistCauldronJeiRecipe(
                recipe.reagent(),
                recipe.fluidIn(),
                recipe.results(),
                recipe.byproduct().orElse(ItemStack.EMPTY)
        );
    }

    private static <T> List<T> allJeiRecipes(IRecipeManager recipeManager, RecipeType<T> type) {
        return recipeManager.createRecipeLookup(type)
                .includeHidden()
                .get()
                .toList();
    }

    private static List<AlchemistCauldronJeiRecipe> uniqueAlchemistRecipes(List<AlchemistCauldronJeiRecipe> recipes) {
        var unique = new ArrayList<AlchemistCauldronJeiRecipe>();
        for (var recipe : recipes) {
            if (!matchesAny(unique, recipe)) {
                unique.add(recipe);
            }
        }
        return unique;
    }

    private static List<AlchemistCauldronJeiRecipe> duplicateAlchemistRecipes(List<AlchemistCauldronJeiRecipe> recipes) {
        var unique = new ArrayList<AlchemistCauldronJeiRecipe>();
        var duplicates = new ArrayList<AlchemistCauldronJeiRecipe>();
        for (var recipe : recipes) {
            if (matchesAny(unique, recipe)) {
                duplicates.add(recipe);
            } else {
                unique.add(recipe);
            }
        }
        return duplicates;
    }

    private static boolean matchesAny(List<AlchemistCauldronJeiRecipe> recipes, AlchemistCauldronJeiRecipe recipe) {
        for (var candidate : recipes) {
            if (sameAlchemistRecipe(candidate, recipe)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sameAlchemistRecipe(AlchemistCauldronJeiRecipe left, AlchemistCauldronJeiRecipe right) {
        return sameIngredient(left.itemIn(), right.itemIn())
                && sameFluidStack(left.fluidIn(), right.fluidIn())
                && sameFluidStacks(left.results(), right.results())
                && sameItemStack(left.resultByproduct(), right.resultByproduct());
    }

    private static boolean sameIngredient(Ingredient left, Ingredient right) {
        var leftItems = left.getItems();
        var rightItems = right.getItems();
        if (leftItems.length != rightItems.length) {
            return false;
        }
        var matched = new boolean[rightItems.length];
        for (var leftItem : leftItems) {
            var found = false;
            for (int i = 0; i < rightItems.length; i++) {
                if (!matched[i] && sameItemStack(leftItem, rightItems[i])) {
                    matched[i] = true;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameItemStack(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return left.isEmpty() && right.isEmpty();
        }
        return left.getCount() == right.getCount() && ItemStack.isSameItemSameTags(left, right);
    }

    private static boolean sameFluidStacks(List<FluidStack> left, List<FluidStack> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int i = 0; i < left.size(); i++) {
            if (!sameFluidStack(left.get(i), right.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean sameFluidStack(FluidStack left, FluidStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return left.isEmpty() && right.isEmpty();
        }
        return left.getAmount() == right.getAmount() && left.isFluidStackIdentical(right);
    }
}
