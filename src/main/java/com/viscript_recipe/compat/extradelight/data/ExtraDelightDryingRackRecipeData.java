package com.viscript_recipe.compat.extradelight.data;

import com.lance5057.extradelight.workstations.dryingrack.DryingRackRecipe;
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

import static com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.requiredItem;

/** Extra Delight 原生晾晒架配方的数据，默认值由接口统一初始化。 */
@Getter
@Setter
@Accessors(chain = true)
public final class ExtraDelightDryingRackRecipeData extends ExtraDelightRecipeData {
    @Persisted private String group = "";
    @Persisted private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.WHEAT)));
    @Persisted private ItemStack result = new ItemStack(Items.BREAD);
    @Persisted private int time = 200;
    @Persisted private float experience = 0;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return new DryingRackRecipe(group, compileIngredient(0), requiredItem(result), Math.max(0, experience), Math.max(1, time));
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        ingredients = new ArrayList<>(List.of(RecipeIngredient.item(Items.KELP)));
        result = new ItemStack(Items.DRIED_KELP);
    }
}
