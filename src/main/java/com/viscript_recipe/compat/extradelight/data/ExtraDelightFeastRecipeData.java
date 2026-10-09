package com.viscript_recipe.compat.extradelight.data;

import com.lance5057.extradelight.recipe.FeastRecipe;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.*;

/** Extra Delight 原生盛宴分装配方的数据，默认值由接口统一初始化。 */
@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightFeastRecipeData extends ExtraDelightRecipeData {
    @Persisted private String group = "";
    @Persisted private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.BOWL)));
    @Persisted private ItemStack feast = new ItemStack(Items.CAKE);
    @Persisted private ItemStack result = new ItemStack(Items.BREAD);

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return new FeastRecipe(group, blockItem(feast), compileIngredient(0), requiredItem(result));
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        feast = item("curry_feast");
        result = item("curry");
    }
}
