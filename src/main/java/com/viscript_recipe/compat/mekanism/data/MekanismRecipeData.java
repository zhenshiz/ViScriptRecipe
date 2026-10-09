package com.viscript_recipe.compat.mekanism.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.mekanism.MekanismRecipeFactory;
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

@Getter
@Setter
@Accessors(chain = true)
public class MekanismRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient itemInput = RecipeIngredient.item(Items.COBBLESTONE);
    @Persisted
    private RecipeIngredient extraItemInput = RecipeIngredient.item(Items.COBBLESTONE);

    @Persisted
    private FluidIngredientData fluidInput = FluidIngredientData.of();
    @Persisted
    private MekanismChemicalIngredientData chemicalInput = new MekanismChemicalIngredientData();
    @Persisted
    private MekanismChemicalIngredientData extraChemicalInput = new MekanismChemicalIngredientData();

    @Persisted
    private ItemStack itemOutput = new ItemStack(Items.IRON_INGOT);
    @Persisted
    private ItemStack secondaryItemOutput = ItemStack.EMPTY;
    @Persisted
    private float secondaryChance;
    @Persisted
    private FluidStack fluidOutput = new FluidStack(Fluids.WATER, 1000);
    @Persisted
    private MekanismChemicalStackData chemicalOutput = new MekanismChemicalStackData();
    @Persisted
    private MekanismChemicalStackData secondaryChemicalOutput = new MekanismChemicalStackData();

    @Persisted
    private boolean perTickUsage;
    @Persisted
    private int duration = 100;
    @Persisted
    private long energyRequired;
    @Persisted
    private long energyMultiplier = 1;
    @Persisted
    private long energyOutput = 1000;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return MekanismRecipeFactory.compile(type, this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        MekanismRecipeKind.byType(typeId).ifPresent(this::applyMekanism);
    }

    private void applyMekanism(MekanismRecipeKind kind) {
        setFluidInput(FluidIngredientData.fluid(new FluidStack(Fluids.WATER, 1000)))
                .setChemicalInput(chemicalIngredient("oxygen"))
                .setExtraChemicalInput(chemicalIngredient("hydrogen"))
                .setChemicalOutput(chemicalOutput("hydrogen"))
                .setSecondaryChemicalOutput(chemicalOutput("oxygen"));
        if (kind == MekanismRecipeKind.SAWING) setItemOutput(new ItemStack(Items.STICK));
    }

    static MekanismChemicalIngredientData chemicalIngredient(String path) {
        return new MekanismChemicalIngredientData().setChemical(ResourceLocation.fromNamespaceAndPath("mekanism", path));
    }

    static MekanismChemicalStackData chemicalOutput(String path) {
        return new MekanismChemicalStackData().setChemical(ResourceLocation.fromNamespaceAndPath("mekanism", path));
    }
}
