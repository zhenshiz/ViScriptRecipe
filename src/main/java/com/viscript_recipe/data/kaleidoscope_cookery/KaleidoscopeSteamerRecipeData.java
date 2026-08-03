package com.viscript_recipe.data.kaleidoscope_cookery;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.kaleidoscope_cookery.KaleidoscopeCookeryRecipeFactory;
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
public class KaleidoscopeSteamerRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.WHEAT);
    @Persisted
    private ItemStack result = new ItemStack(Items.BREAD);
    @Persisted
    private int cookTick = 1200;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return KaleidoscopeCookeryRecipeFactory.compileSteamer(this);
    }
}
