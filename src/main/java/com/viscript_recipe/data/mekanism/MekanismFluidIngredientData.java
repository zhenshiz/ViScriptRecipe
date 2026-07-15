package com.viscript_recipe.data.mekanism;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigRL;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

@Getter
@Setter
@Accessors(chain = true)
public class MekanismFluidIngredientData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.mekanism.fluid_ingredient.kind")
    @ConfigSelector(candidate = {"fluid", "tag"})
    private MekanismFluidIngredientKind kind = MekanismFluidIngredientKind.FLUID;

    @Configurable(name = "viscript_recipe.config.mekanism.fluid_ingredient.fluid")
    private FluidStack fluid = new FluidStack(Fluids.WATER, 1000);

    @Configurable(name = "viscript_recipe.config.mekanism.fluid_ingredient.tag")
    @ConfigRL(ConfigRL.Type.FLUID_TAG_KEY)
    private ResourceLocation tag = ResourceLocation.fromNamespaceAndPath("c", "water");

    @Configurable(name = "viscript_recipe.config.mekanism.fluid_ingredient.amount")
    private int amount = 1000;
}
