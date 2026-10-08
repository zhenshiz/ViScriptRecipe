package com.viscript_recipe.compat.extradelight.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.ArrayList;
import java.util.List;
import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.*;

import com.lance5057.extradelight.recipe.BottleFluidRegistryRecipe;

/** Extra Delight 原生瓶装流体映射配方的数据，默认值由接口统一初始化。 */
@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightBottleFluidRecipeData extends ExtraDelightRecipeData {
    @Persisted private String group = "";
    @Persisted private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.POTION)));
    @Persisted private FluidIngredientData fluid = FluidIngredientData.fluid(new FluidStack(Fluids.WATER, 250));

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return new BottleFluidRegistryRecipe(group, compileIngredient(0), compileFluid(0, fluid));
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.POTION)));
    }
}
