package com.viscript_recipe.compat.extradelight;

import com.lance5057.extradelight.workstations.vat.recipes.VatRecipe;
import com.viscript_recipe.compat.extradelight.data.*;
import com.viscript_recipe.data.*;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.*;

/** 转换 VSR 的通用槽位数据与 Extra Delight 原生配方字段。 */
public final class ExtraDelightRecipeSupport {
    private ExtraDelightRecipeSupport() {}

    public static ItemStack item(String path) {
        var id = ResourceLocation.fromNamespaceAndPath("extradelight", path);
        if (!net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(id))
            throw new IllegalArgumentException("Missing Extra Delight default item: " + id);
        return new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(id));
    }

    public static FluidStack fluidStack(String path, int amount) {
        var id = ResourceLocation.parse("extradelight:" + path);
        return new FluidStack(net.minecraft.core.registries.BuiltInRegistries.FLUID.get(id), amount);
    }

    public static ItemStack requiredItem(ItemStack stack) {
        if (stack.isEmpty()) throw new IllegalArgumentException("Extra Delight result cannot be empty");
        return stack.copy();
    }

    public static ItemStack blockItem(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem)) throw new IllegalArgumentException("Extra Delight requires a block item");
        return stack.copyWithCount(1);
    }

    public static FluidStack requiredFluid(FluidStack stack) {
        if (stack.isEmpty()) throw new IllegalArgumentException("Extra Delight fluid cannot be empty");
        return stack.copy();
    }

    public static SizedFluidIngredient sizedFluid(FluidIngredientData data) {
        if (data.isEmpty()) throw new IllegalArgumentException("Extra Delight fluid ingredient cannot be empty");
        return data.getKind() == FluidIngredientKind.TAG
                ? SizedFluidIngredient.of(TagKey.create(Registries.FLUID, data.getTag()), data.getAmount())
                : SizedFluidIngredient.of(data.getFluid().copyWithAmount(data.getAmount()));
    }

    public static FluidIngredientData importFluid(SizedFluidIngredient data) throws RecipeImportException {
        if (data.ingredient() instanceof TagFluidIngredient tag)
            return FluidIngredientData.tag(tag.tag().location()).setAmount(data.amount());
        if (data.ingredient() instanceof SingleFluidIngredient single)
            return FluidIngredientData.fluid(new FluidStack(single.fluid(), data.amount()));
        var stacks = data.getFluids();
        if (stacks.length == 0) throw new RecipeImportException(ExtraDelightRecipeEditorTypes.key("empty_fluid_ingredient"));
        return FluidIngredientData.fluid(stacks[0].copyWithAmount(data.amount()));
    }

    public static VatRecipe compileVat(ExtraDelightVatRecipeData data) {
        var stages = NonNullList.<VatRecipe.StageIngredient>create();
        for (var stage : data.getStages()) stages.add(new VatRecipe.StageIngredient(
                stage.getIngredientCondition().isBlank() ? stage.getIngredient().compile() : ExtraDelightRecipeData.decodeIngredient(stage.getIngredientCondition()), Math.max(1, stage.getTime()), stage.isLid()));
        if (stages.isEmpty())
            throw new IllegalArgumentException("Extra Delight vat requires at least one stage");
        return new VatRecipe(data.getGroup(), data.compileIngredients(6, true), stages,
                data.compileFluid(0, data.getFluid()), requiredItem(data.getResult()), stages.size(), data.getContainer().copy());
    }
}
