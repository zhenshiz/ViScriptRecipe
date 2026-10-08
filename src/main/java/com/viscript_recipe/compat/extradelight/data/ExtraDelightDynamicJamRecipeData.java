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
import java.util.ArrayList;
import java.util.List;
import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.*;

import com.lance5057.extradelight.recipe.DynamicJamRecipe;

/** Extra Delight 原生动态果酱配方的数据，默认值由接口统一初始化。 */
@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightDynamicJamRecipeData extends ExtraDelightRecipeData {
    @Persisted private String group = "";
    @Persisted private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.WHEAT)));
    @Persisted private ItemStack result = new ItemStack(Items.BREAD);
    @Persisted private String graphic = "apple";
    @Persisted private ItemStack container = ItemStack.EMPTY;
    @Persisted private int time = 200;
    @Persisted private float experience = 0;
    @Persisted private String recipeBookTab = "meals";

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return new DynamicJamRecipe(group, vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab.findByName(recipeBookTab),
                compileIngredients(6, false), requiredItem(result), container.copy(), Math.max(0, experience), Math.max(1, time), graphic);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        result = item("dynamic_jam");
        ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.APPLE), RecipeIngredient.item(Items.SUGAR)));
        container = new ItemStack(Items.GLASS_BOTTLE);
    }
}
