package com.viscript_recipe.compat.irons_spellbooks.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.irons_spellbooks.IronSpellbooksRecipeFactory;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.IVSRecipeData;
import net.minecraft.resources.ResourceLocation;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class IronNoAdditionSmithingRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient template = RecipeIngredient.item(Items.GOLD_INGOT);
    @Persisted
    private RecipeIngredient base = RecipeIngredient.item(Items.IRON_SWORD);
    @Persisted
    private ItemStack result = new ItemStack(Items.GOLDEN_SWORD);

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return IronSpellbooksRecipeFactory.compileNoAdditionSmithing(this);
    }
}
