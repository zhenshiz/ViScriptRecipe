package com.viscript_recipe.data.cataclysm;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.cataclysm.CataclysmRecipeFactory;
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
public class CataclysmAmethystBlessRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.AMETHYST_SHARD);
    @Persisted
    private ItemStack result = new ItemStack(Items.AMETHYST_BLOCK);
    @Persisted
    private int time = 200;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return CataclysmRecipeFactory.compileAmethystBless(this);
    }
}
