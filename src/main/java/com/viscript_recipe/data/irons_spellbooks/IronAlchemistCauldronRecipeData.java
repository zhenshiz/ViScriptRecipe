package com.viscript_recipe.data.irons_spellbooks;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.irons_spellbooks.IronSpellbooksRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class IronAlchemistCauldronRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.WATER_BUCKET);
    @Persisted
    private ItemStack result = new ItemStack(Items.BUCKET);
    @Persisted
    private FluidStack fluid = new FluidStack(Fluids.WATER, 1000);
    @Persisted
    private FluidStack baseFluid = new FluidStack(Fluids.WATER, 1000);
    @Persisted
    private List<FluidStack> resultFluids = new ArrayList<>(List.of(new FluidStack(Fluids.WATER, 1000)));
    @Persisted
    private ItemStack byproduct = ItemStack.EMPTY;
    @Persisted
    private boolean mustFitAll = true;
    @Persisted
    private ResourceLocation sound = ResourceLocation.withDefaultNamespace("item.bucket.empty");

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return typeId.equals(IronSpellbooksRecipeEditorTypes.ALCHEMIST_CAULDRON_FILL) ?
                IronSpellbooksRecipeFactory.compileFill(this) : IronSpellbooksRecipeFactory.compileEmpty(this);
    }

    public static class Brew extends IronAlchemistCauldronRecipeData {
        @Override
        public String getDataName() {return "ironAlchemistCauldron";}

        @Override
        public Recipe<?> compile(ResourceLocation typeId) {
            return IronSpellbooksRecipeFactory.compileBrew(this);
        }

        @Override
        public ItemStack getResult() {return ItemStack.EMPTY;}

        @Override
        public IronAlchemistCauldronRecipeData setResult(ItemStack result) {return setByproduct(ItemStack.EMPTY);}
    }

    public FluidStack firstResultFluid() {
        if (resultFluids == null || resultFluids.isEmpty()) {
            return FluidStack.EMPTY;
        }
        var stack = resultFluids.getFirst();
        return stack == null ? FluidStack.EMPTY : stack.copy();
    }

    public FluidStack displayResultFluid() {
        if (resultFluids == null || resultFluids.isEmpty()) {
            return FluidStack.EMPTY;
        }
        for (var stack : resultFluids) {
            if (stack != null && !stack.isEmpty()) {
                return stack.copy();
            }
        }
        return FluidStack.EMPTY;
    }

    public void setFirstResultFluid(FluidStack stack) {
        if (resultFluids == null) {
            resultFluids = new ArrayList<>();
        }
        var copy = stack == null ? FluidStack.EMPTY : stack.copy();
        if (resultFluids.isEmpty()) {
            resultFluids.add(copy);
        } else {
            resultFluids.set(0, copy);
        }
    }

    public void replaceResultFluids(List<FluidStack> stacks) {
        resultFluids = new ArrayList<>();
        if (stacks == null) {
            return;
        }
        for (var stack : stacks) {
            resultFluids.add(stack == null ? FluidStack.EMPTY : stack.copy());
        }
    }
}
