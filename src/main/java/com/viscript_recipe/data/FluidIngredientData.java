package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

@Getter
@Setter
@Accessors(chain = true)
public class FluidIngredientData implements ISkipDefaultedSerialize, IConfigurable {
    @Persisted
    private FluidIngredientKind kind = FluidIngredientKind.FLUID;
    @Persisted
    private FluidStack fluid = new FluidStack(Fluids.WATER, 1000);
    @Persisted
    private ResourceLocation tag = ResourceLocation.fromNamespaceAndPath("c", "water");
    @Persisted
    private int amount = 1000;

    /**请使用工厂方法*/
    private FluidIngredientData() {}

    public static FluidIngredientData of() {return new FluidIngredientData();}
    public static FluidIngredientData empty() {return fluid(FluidStack.EMPTY);}

    public static FluidIngredientData fluid(FluidStack stack) {
        var fluid = stack == null ? FluidStack.EMPTY : stack.copy();
        return of().setKind(FluidIngredientKind.FLUID)
                .setFluid(fluid).setAmount(fluid.isEmpty() ? 0 : Math.max(1, fluid.getAmount()));
    }

    public static FluidIngredientData tag(ResourceLocation tag) {
        return of().setKind(FluidIngredientKind.TAG).setTag(tag);
    }

    public FluidIngredientData copy() {
        return of().setKind(kind).setFluid(fluid.copy()).setTag(tag).setAmount(amount);
    }
}
