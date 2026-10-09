package com.viscript_recipe.compat.extradelight.data;

import com.lance5057.extradelight.workstations.juicer.JuicerRecipe;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
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

import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.fluidStack;

/** Extra Delight 原生榨汁机配方的数据，默认值由接口统一初始化。 */
@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightJuicerRecipeData extends ExtraDelightRecipeData {
    @Persisted private String group = "";
    @Persisted private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.WHEAT)));
    @Persisted private ItemStack result = new ItemStack(Items.RED_DYE);
    @Persisted private FluidStack fluidOutput = new FluidStack(Fluids.WATER, 250);
    @Persisted private int chance = 25;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return new JuicerRecipe(group, compileIngredient(0), result.copy(), Math.clamp(chance, 0, 100), fluidOutput.copy());
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.APPLE)));
        fluidOutput = fluidStack("apple_cider_fluid", 250);
    }
}
