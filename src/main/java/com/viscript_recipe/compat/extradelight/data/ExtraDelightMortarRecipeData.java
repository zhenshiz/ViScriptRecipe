package com.viscript_recipe.compat.extradelight.data;

import com.lance5057.extradelight.workstations.mortar.recipes.MortarRecipe;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.item;

/** Extra Delight 原生研钵配方的数据，默认值由接口统一初始化。 */
@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightMortarRecipeData extends ExtraDelightRecipeData {
    @Persisted private String group = "";
    @Persisted private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.WHEAT)));
    @Persisted private ItemStack result = new ItemStack(Items.BREAD);
    @Persisted private FluidStack fluidOutput = FluidStack.EMPTY;
    @Persisted private int grinds = 4;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return new MortarRecipe(group, compileIngredient(0), result.copy(), fluidOutput.copy(), Math.max(1, grinds));
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.WHEAT)));
        result = item("flour");
    }
}
