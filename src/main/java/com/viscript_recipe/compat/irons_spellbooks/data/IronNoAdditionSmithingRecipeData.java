package com.viscript_recipe.compat.irons_spellbooks.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.irons_spellbooks.IronSpellbooksRecipeFactory;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class IronNoAdditionSmithingRecipeData implements IPersistedSerializable, IConfigurable {
    @Persisted
    private RecipeIngredient template = RecipeIngredient.item(Items.GOLD_INGOT);
    @Persisted
    private RecipeIngredient base = RecipeIngredient.item(Items.IRON_SWORD);
    @Persisted
    private ItemStack result = new ItemStack(Items.GOLDEN_SWORD);

    public Recipe<?> compile() {
        return IronSpellbooksRecipeFactory.compileNoAdditionSmithing(this);
    }
}
