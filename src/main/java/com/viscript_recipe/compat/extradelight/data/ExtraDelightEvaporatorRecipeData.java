package com.viscript_recipe.compat.extradelight.data;

import com.lance5057.extradelight.workstations.evaporator.recipes.EvaporatorRecipe;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.FluidIngredientData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.item;
import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.requiredItem;

/** Extra Delight 原生蒸发器配方的数据，默认值由接口统一初始化。 */
@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightEvaporatorRecipeData extends ExtraDelightRecipeData {
    @Persisted private String group = "";
    @Persisted private FluidIngredientData fluid = FluidIngredientData.fluid(new FluidStack(Fluids.WATER, 250));
    @Persisted private ItemStack result = new ItemStack(Items.SUGAR);
    @Persisted private int time = 200;
    @Persisted private ResourceLocation lootTable = ResourceLocation.parse("extradelight:evaporator/water");
    @Persisted private ResourceLocation displayBlock = ResourceLocation.parse("extradelight:salt_block");

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return new EvaporatorRecipe(group, compileFluid(0, fluid), Math.max(1, time), lootTable, displayBlock, requiredItem(result));
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        fluid = FluidIngredientData.fluid(new FluidStack(Fluids.WATER, 1000));
        result = item("salt");
        time = 10000;
    }
}
