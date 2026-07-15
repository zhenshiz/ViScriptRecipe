package com.viscript_recipe.data.industrial_foregoing;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSelector;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/** Stores the flattened {@code SizedFluidIngredient} fields used by Industrial Foregoing codecs. */
@Getter
@Setter
@Accessors(chain = true)
public class IndustrialFluidIngredientData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.industrial_foregoing.fluid_ingredient.kind")
    @ConfigSelector(candidate = {"fluid", "tag"})
    private IndustrialFluidIngredientKind kind = IndustrialFluidIngredientKind.FLUID;

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.fluid_ingredient.fluid")
    private FluidStack fluid = new FluidStack(Fluids.WATER, 1000);

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.fluid_ingredient.tag")
    private ResourceLocation tag = ResourceLocation.fromNamespaceAndPath("c", "water");

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.fluid_ingredient.amount")
    private int amount = 1000;
}
