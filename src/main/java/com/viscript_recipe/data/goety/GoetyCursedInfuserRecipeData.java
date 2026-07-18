package com.viscript_recipe.data.goety;

import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.viscript_recipe.compat.goety.GoetyRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class GoetyCursedInfuserRecipeData implements IVSRecipeData {
    @Configurable(name = "viscript_recipe.config.goety.cursed_infuser.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.EMERALD_BLOCK);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.DIAMOND_BLOCK);

    @Configurable(name = "viscript_recipe.config.goety.cursed_infuser.cooking_time")
    private int cookingTime = 60;

    @Configurable(name = "viscript_recipe.config.goety.cursed_infuser.grim")
    private boolean grim;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return GoetyRecipeFactory.compileCursedInfuser(this);
    }
}
