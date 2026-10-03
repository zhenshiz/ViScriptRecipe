package com.viscript_recipe.compat.irons_spellbooks.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Getter
@Setter
@Accessors(chain = true)
public class IronArcaneAnvilRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.IRON_SWORD);
    @Persisted
    private RecipeIngredient material = RecipeIngredient.item(Items.AMETHYST_SHARD);
    @Persisted
    private ItemStack result = new ItemStack(Items.DIAMOND_SWORD);
}
