package com.viscript_recipe.compat.extradelight.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.ArrayList;
import java.util.List;
import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.*;

import com.lance5057.extradelight.workstations.chiller.ChillerRecipe;

/** Extra Delight 原生冷却器配方的数据，默认值由接口统一初始化。 */
@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightChillerRecipeData extends ExtraDelightRecipeData {
    @Persisted private String group = "";
    @Persisted private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.WHEAT)));
    @Persisted private ItemStack result = new ItemStack(Items.BREAD);
    @Persisted private FluidStack fluid = new FluidStack(Fluids.WATER, 250);
    @Persisted private ItemStack container = ItemStack.EMPTY;
    @Persisted private int time = 200;
    @Persisted private float experience = 0;
    @Persisted private boolean consumeContainer = false;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return new ChillerRecipe(group, compileIngredients(4, true), fluid.copy(), requiredItem(result), container.copy(), Math.max(0, experience), Math.max(1, time), consumeContainer);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        ingredients = new ArrayList<>();
        result = new ItemStack(Items.ICE);
    }
}
