package com.viscript_recipe.data.mekanism;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.compat.mekanism.MekanismRecipeFactory;
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
public class MekanismRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.mekanism.item_input", subConfigurable = true)
    private RecipeIngredient itemInput = RecipeIngredient.item(Items.COBBLESTONE);
    @Configurable(name = "viscript_recipe.config.mekanism.extra_item_input", subConfigurable = true)
    private RecipeIngredient extraItemInput = RecipeIngredient.item(Items.COBBLESTONE);
    @Configurable(name = "viscript_recipe.config.mekanism.item_input_amount")
    private int itemInputAmount = 1;
    @Configurable(name = "viscript_recipe.config.mekanism.extra_item_input_amount")
    private int extraItemInputAmount = 1;

    @Configurable(name = "viscript_recipe.config.mekanism.fluid_input", subConfigurable = true)
    private MekanismFluidIngredientData fluidInput = new MekanismFluidIngredientData();
    @Configurable(name = "viscript_recipe.config.mekanism.chemical_input", subConfigurable = true)
    private MekanismChemicalIngredientData chemicalInput = new MekanismChemicalIngredientData();
    @Configurable(name = "viscript_recipe.config.mekanism.extra_chemical_input", subConfigurable = true)
    private MekanismChemicalIngredientData extraChemicalInput = new MekanismChemicalIngredientData()
            .setChemical(ResourceLocation.fromNamespaceAndPath("mekanism", "hydrogen"));

    @Configurable(name = "viscript_recipe.config.mekanism.item_output")
    private ItemStack itemOutput = new ItemStack(Items.IRON_INGOT);
    @Configurable(name = "viscript_recipe.config.mekanism.secondary_item_output")
    private ItemStack secondaryItemOutput = ItemStack.EMPTY;
    @Configurable(name = "viscript_recipe.config.mekanism.secondary_chance")
    private double secondaryChance;
    @Configurable(name = "viscript_recipe.config.mekanism.fluid_output")
    private FluidStack fluidOutput = new FluidStack(Fluids.WATER, 1000);
    @Configurable(name = "viscript_recipe.config.mekanism.chemical_output", subConfigurable = true)
    private MekanismChemicalStackData chemicalOutput = new MekanismChemicalStackData();
    @Configurable(name = "viscript_recipe.config.mekanism.secondary_chemical_output", subConfigurable = true)
    private MekanismChemicalStackData secondaryChemicalOutput = new MekanismChemicalStackData()
            .setChemical(ResourceLocation.fromNamespaceAndPath("mekanism", "oxygen"));

    @Configurable(name = "viscript_recipe.config.mekanism.per_tick_usage")
    private boolean perTickUsage;
    @Configurable(name = "viscript_recipe.config.mekanism.duration")
    private int duration = 100;
    @Configurable(name = "viscript_recipe.config.mekanism.energy_required")
    private long energyRequired;
    @Configurable(name = "viscript_recipe.config.mekanism.energy_multiplier")
    private long energyMultiplier = 1;
    @Configurable(name = "viscript_recipe.config.mekanism.energy_output")
    private long energyOutput = 1000;
    public Recipe<?> compile(ResourceLocation type) {
        return MekanismRecipeFactory.compile(type, this);
    }
}
