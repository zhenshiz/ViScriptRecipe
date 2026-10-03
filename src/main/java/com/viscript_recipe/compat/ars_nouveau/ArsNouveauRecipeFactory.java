package com.viscript_recipe.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.api.enchanting_apparatus.*;
import com.hollingsworth.arsnouveau.common.crafting.recipes.CrushRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.GlyphRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ImbuementRecipe;
import com.viscript_lib.util.math.Clamp;
import com.viscript_recipe.compat.ars_nouveau.data.*;
import com.viscript_recipe.data.RecipeIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ArsNouveauRecipeFactory {
    private static final int MAX_PEDESTAL_ITEMS = 8;
    private static final int MAX_GLYPH_INPUTS = 9;
    private static final int MAX_CRUSH_OUTPUTS = 6;

    private ArsNouveauRecipeFactory() {
    }

    public static Recipe<?> compileApparatus(ResourceLocation recipeId, ArsNouveauApparatusRecipeData data) {
        var reagent = compileIngredient(data.getReagent());
        if (reagent.isEmpty()) {
            throw new IllegalArgumentException("Ars Nouveau enchanting apparatus recipe must have a reagent");
        }
        return new EnchantingApparatusRecipe(
                recipeId, compileIngredients(data.getPedestalItems(), MAX_PEDESTAL_ITEMS), reagent,
                requireItem(data.getResult(), "Ars Nouveau enchanting apparatus result cannot be empty"),
                Math.max(0, data.getSourceCost()),
                data.isKeepNbtOfReagent()
        );
    }

    public static Recipe<?> compileArmorUpgrade(ResourceLocation recipeId, ArsNouveauArmorUpgradeRecipeData data) {
        var pedestalItems = compileIngredients(data.getPedestalItems(), MAX_PEDESTAL_ITEMS);
        if (pedestalItems.isEmpty()) {
            throw new IllegalArgumentException("Ars Nouveau armor upgrade recipe must have at least one pedestal item");
        }
        return new ArmorUpgradeRecipe(recipeId, pedestalItems, data.getSourceCost(), data.getTier());
    }

    public static Recipe<?> compileEnchantment(ResourceLocation recipeId, ArsNouveauEnchantmentRecipeData data) {
        var pedestalItems = compileIngredients(data.getPedestalItems(), MAX_PEDESTAL_ITEMS);
        if (pedestalItems.isEmpty()) {
            throw new IllegalArgumentException("Ars Nouveau enchantment recipe must have at least one pedestal item");
        }
        var enchantment = data.getEnchantment() == null ? new ResourceLocation("sharpness") : data.getEnchantment();
        var recipe = new EnchantmentRecipe(
                pedestalItems,
                BuiltInRegistries.ENCHANTMENT.get(enchantment),
                Math.max(1, data.getLevel()),
                Math.max(0, data.getSourceCost())
        );
        recipe.id = recipeId;
        return recipe;
    }

    public static Recipe<?> compileImbuement(ResourceLocation recipeId, ArsNouveauImbuementRecipeData data) {
        var input = compileIngredient(data.getInput());
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Ars Nouveau imbuement recipe must have an input");
        }
        return new ImbuementRecipe(
                recipeId, input,
                requireItem(data.getResult(), "Ars Nouveau imbuement result cannot be empty"),
                Math.max(0, data.getSource()),
                compileIngredients(data.getPedestalItems(), MAX_PEDESTAL_ITEMS)
        );
    }

    public static Recipe<?> compileGlyph(ResourceLocation recipeId, ArsNouveauGlyphRecipeData data) {
        var inputs = compileIngredients(data.getInputs(), MAX_GLYPH_INPUTS);
        if (inputs.isEmpty()) {
            throw new IllegalArgumentException("Ars Nouveau glyph recipe must have at least one input");
        }
        return new GlyphRecipe(
                recipeId,
                requireItem(data.getResult(), "Ars Nouveau glyph result cannot be empty"),
                inputs,
                Math.max(0, data.getExp())
        );
    }

    public static Recipe<?> compileCrush(ResourceLocation recipeId, ArsNouveauCrushRecipeData data) {
        var input = compileIngredient(data.getInput());
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Ars Nouveau crush recipe must have an input");
        }
        var outputs = compileCrushOutputs(data.getOutputs());
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("Ars Nouveau crush recipe must have at least one output");
        }
        return new CrushRecipe(recipeId, input, outputs, data.isSkipBlockPlace());
    }

    public static Recipe<?> compilePedestalOnly(ResourceLocation recipeId, ResourceLocation type, ArsNouveauPedestalOnlyRecipeData data) {
        var pedestalItems = compileIngredients(data.getPedestalItems(), MAX_PEDESTAL_ITEMS);
        if (pedestalItems.isEmpty()) {
            throw new IllegalArgumentException("Ars Nouveau pedestal recipe must have at least one pedestal item");
        }
        var sourceCost = Math.max(0, data.getSourceCost());
        if (ArsNouveauRecipeEditorTypes.REACTIVE_ENCHANTMENT.equals(type)) {
            var recipe = new ReactiveEnchantmentRecipe(pedestalItems, sourceCost);
            recipe.id = recipeId;
            return recipe;
        }
        if (ArsNouveauRecipeEditorTypes.SPELL_WRITE.equals(type)) {
            return new SpellWriteRecipe(recipeId, pedestalItems, sourceCost);
        }
        /*if (ArsNouveauRecipeEditorTypes.PRESTIDIGITATION.equals(type)) {
            return new PrestidigitationRecipe(pedestalItems, sourceCost);
        }*/
        throw new IllegalArgumentException("Unsupported Ars Nouveau pedestal recipe type: " + type);
    }

    private static List<Ingredient> compileIngredients(List<RecipeIngredient> ingredients, int maxCount) {
        var compiled = new ArrayList<Ingredient>();
        for (var ingredientData : safeList(ingredients)) {
            if (compiled.size() >= maxCount) {
                break;
            }
            var ingredient = compileIngredient(ingredientData);
            if (!ingredient.isEmpty()) {
                compiled.add(ingredient);
            }
        }
        return compiled;
    }

    private static List<CrushRecipe.CrushOutput> compileCrushOutputs(List<ArsNouveauCrushOutputData> outputs) {
        var compiled = new ArrayList<CrushRecipe.CrushOutput>();
        for (var output : safeList(outputs)) {
            if (compiled.size() >= MAX_CRUSH_OUTPUTS) {
                break;
            }
            var stack = normalizeOutput(output == null ? ItemStack.EMPTY : output.getItem());
            if (!stack.isEmpty()) {
                compiled.add(new CrushRecipe.CrushOutput(
                        stack,
                        Clamp.clamp(output.getChance(), 0, 1),
                        Math.max(1, output.getMaxRange())
                ));
            }
        }
        return compiled;
    }

    private static Ingredient compileIngredient(RecipeIngredient ingredient) {
        return ingredient == null ? Ingredient.EMPTY : ingredient.compile();
    }

    private static ItemStack requireItem(ItemStack stack, String message) {
        var copy = normalizeOutput(stack);
        if (copy.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return copy;
    }

    private static ItemStack normalizeOutput(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            return ItemStack.EMPTY;
        }
        var copy = stack.copy();
        copy.setCount(Clamp.clamp(copy.getCount(), 1, 99));
        return copy;
    }

    private static <T> List<T> safeList(List<T> list) {
        return list == null ? List.of() : list.stream().filter(Objects::nonNull).toList();
    }
}
