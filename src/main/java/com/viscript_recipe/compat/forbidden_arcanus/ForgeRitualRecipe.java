package com.viscript_recipe.compat.forbidden_arcanus;

import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.Ritual;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.CreateItemResult;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.TransmuteInputResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/** 将仪式接入 VSR 的配方应用和同步流程，仪式执行仍由锻造炉负责。 */
public record ForgeRitualRecipe(Ritual ritual) implements Recipe<SingleRecipeInput> {
    @Override
    public boolean matches(SingleRecipeInput input, Level level) {return false;}

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider provider) {
        return ritual.result().getResultItem(input.item());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {return false;}

    @Override
    public boolean isSpecial() {return true;}

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return switch (ritual.result()) {
            case CreateItemResult item -> item.result();
            case TransmuteInputResult item -> item.result().value().getDefaultInstance();
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public RecipeSerializer<?> getSerializer() {return ForgeRitualRecipes.SERIALIZER.get();}

    @Override
    public RecipeType<?> getType() {return ForgeRitualRecipes.TYPE.get();}
}
