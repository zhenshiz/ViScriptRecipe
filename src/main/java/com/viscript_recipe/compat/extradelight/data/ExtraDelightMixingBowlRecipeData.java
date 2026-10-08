package com.viscript_recipe.compat.extradelight.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.*;
import com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import java.util.ArrayList;
import java.util.List;
import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.*;

import com.lance5057.extradelight.workstations.mixingbowl.recipes.MixingBowlRecipe;

/** Extra Delight 原生搅拌碗配方的数据，默认值由接口统一初始化。 */
@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightMixingBowlRecipeData extends ExtraDelightRecipeData {
    @Persisted private String group = "";
    @Persisted private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.WHEAT)));
    @Persisted private ItemStack result = new ItemStack(Items.BREAD);
    @Persisted private List<FluidIngredientData> fluids = new ArrayList<>();
    @Persisted private ItemStack container = ItemStack.EMPTY;
    @Persisted private RecipeIngredient utensil = RecipeIngredient.tag("c:spoons");
    @Persisted private String utensilCondition = "";
    @Persisted private int stirs = 2;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return new MixingBowlRecipe(group, compileIngredients(9, true), java.util.stream.IntStream.range(0, fluids.size()).mapToObj(i -> compileFluid(i, fluids.get(i))).toList(), requiredItem(result), Math.max(1, stirs), container.copy(), utensilCondition.isBlank() ? utensil.compile() : decodeIngredient(utensilCondition));
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.WHEAT), RecipeIngredient.item(Items.SUGAR)));
    }
}
