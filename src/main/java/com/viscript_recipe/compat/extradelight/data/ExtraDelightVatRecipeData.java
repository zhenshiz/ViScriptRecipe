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


/** Extra Delight 原生发酵缸配方的数据，默认值由接口统一初始化。 */
@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightVatRecipeData extends ExtraDelightRecipeData {
    @Persisted private String group = "";
    @Persisted private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.WHEAT)));
    @Persisted private ItemStack result = new ItemStack(Items.BREAD);
    @Persisted private FluidIngredientData fluid = FluidIngredientData.fluid(new FluidStack(Fluids.WATER, 250));
    @Persisted private ItemStack container = ItemStack.EMPTY;
    @Persisted private List<ExtraDelightVatStageData> stages = new ArrayList<>(List.of(new ExtraDelightVatStageData()));

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return compileVat(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        container = new ItemStack(Items.GLASS_BOTTLE);
        result = item("soy_sauce_item");
    }
}
