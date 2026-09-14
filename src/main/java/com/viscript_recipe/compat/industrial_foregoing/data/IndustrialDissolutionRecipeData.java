package com.viscript_recipe.compat.industrial_foregoing.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.industrial_foregoing.IndustrialForegoingRecipeFactory;
import com.viscript_recipe.data.FluidIngredientData;
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
public class IndustrialDissolutionRecipeData implements IVSRecipeData {
    public static final int MAX_INPUTS = 8;

    @Persisted
    private List<RecipeIngredient> input = new ArrayList<>(List.of(RecipeIngredient.item(Items.IRON_INGOT)));
    @Persisted
    private FluidIngredientData inputFluid = FluidIngredientData.of();
    @Persisted
    private int processingTime = 300;
    @Persisted
    private boolean hasItemOutput = true;
    @Persisted
    private ItemStack output = new ItemStack(Items.DIAMOND);
    @Persisted
    private boolean hasFluidOutput;
    @Persisted
    private FluidStack outputFluid = new FluidStack(Fluids.WATER, 1000);

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return IndustrialForegoingRecipeFactory.compileDissolution(this);
    }
}
