package com.viscript_recipe.data.irons_spellbooks;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
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
public class IronArcaneAnvilRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.IRON_SWORD);
    @Persisted
    private RecipeIngredient material = RecipeIngredient.item(Items.AMETHYST_SHARD);
    @Persisted
    private ItemStack result = new ItemStack(Items.DIAMOND_SWORD);

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        throw new UnsupportedOperationException("Iron's Spells Arcane Anvil recipes are handled by ViScriptRecipe's Arcane Anvil menu hook");
    }
}
