package com.viscript_recipe.compat.forbidden_arcanus.data;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.stal111.forbidden_arcanus.common.block.entity.clibano.ClibanoCookingTimes;
import com.stal111.forbidden_arcanus.common.block.entity.clibano.ClibanoFireType;
import com.stal111.forbidden_arcanus.common.block.entity.clibano.residue.ResidueChance;
import com.stal111.forbidden_arcanus.common.block.entity.clibano.residue.ResidueType;
import com.stal111.forbidden_arcanus.common.item.crafting.ClibanoRecipe;
import com.stal111.forbidden_arcanus.common.item.enhancer.EnhancerDefinition;
import com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeValues;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.NativeIngredientData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Optional;

import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeValues.requireIngredient;

@Getter
@Setter
@Accessors(chain = true)
public final class ClibanoRecipeData implements IVSRecipeData {
    @Persisted private NativeIngredientData input = new NativeIngredientData().setValue(RecipeIngredient.item(Items.RAW_IRON));
    @Persisted private NativeIngredientData secondInput = new NativeIngredientData();
    @Persisted private ItemStack result = new ItemStack(Items.IRON_INGOT);
    @Persisted private String group = "";
    @Persisted private CookingBookCategory category = CookingBookCategory.MISC;
    @Persisted private int cookingTime = 100;
    @Persisted private float experience = 0.35f;
    @Persisted private ClibanoFireType fire = ClibanoFireType.FIRE;
    @Persisted private String enhancer = "";
    @Persisted private String residue = "";
    @Persisted private double residueChance = 0.33;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {return compile(typeId, Platform.getFrozenRegistry());}

    @Override
    public ClibanoRecipe compile(ResourceLocation typeId, HolderLookup.Provider provider) {
        if (result.isEmpty() || cookingTime < 1 || !Float.isFinite(experience) || experience < 0) {
            throw new IllegalArgumentException("Clibano combustion requires an output, positive cooking time and nonnegative experience");
        }
        var first = requireIngredient(input, provider);
        var second = secondInput.compile(provider);
        Either<Ingredient, Pair<Ingredient, Ingredient>> ingredients = second.isEmpty()
                ? Either.left(first) : Either.right(Pair.of(first, second));
        Optional<Holder<EnhancerDefinition>> requiredEnhancer = enhancer.isBlank() ? Optional.empty()
                : Optional.of(ForbiddenArcanusRecipeValues.decode(EnhancerDefinition.REFERENCE_CODEC, provider, enhancer));
        Optional<ResidueChance> chance = Optional.empty();
        if (!residue.isBlank()) {
            if (!Double.isFinite(residueChance) || residueChance < 0 || residueChance > 1) {
                throw new IllegalArgumentException("Clibano residue probability must be in [0, 1]");
            }
            chance = Optional.of(new ResidueChance(
                    ForbiddenArcanusRecipeValues.decode(ResidueType.CODEC, provider, residue), residueChance));
        }
        return new ClibanoRecipe(group, category, ingredients, result.copy(), experience,
                ClibanoCookingTimes.of(cookingTime), chance, fire, requiredEnhancer);
    }
}
