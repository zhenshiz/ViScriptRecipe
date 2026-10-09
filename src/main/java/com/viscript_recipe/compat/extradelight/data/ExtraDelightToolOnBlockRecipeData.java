package com.viscript_recipe.compat.extradelight.data;

import com.lance5057.extradelight.recipe.ToolOnBlockRecipe;
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

import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.blockItem;

/** Extra Delight 原生工具处理方块配方的数据，默认值由接口统一初始化。 */
@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightToolOnBlockRecipeData extends ExtraDelightRecipeData {
    @Persisted private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.OAK_LOG), RecipeIngredient.item(Items.IRON_AXE)));
    @Persisted private ItemStack result = new ItemStack(Items.STRIPPED_OAK_LOG);

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return new ToolOnBlockRecipe(blockItem(ingredient(0).toStack()), compileIngredient(1), blockItem(result));
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.OAK_LOG), RecipeIngredient.item(Items.IRON_AXE)));
    }
}
