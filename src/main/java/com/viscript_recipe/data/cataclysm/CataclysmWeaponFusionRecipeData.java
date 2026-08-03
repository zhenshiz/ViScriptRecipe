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
public class CataclysmWeaponFusionRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient base = RecipeIngredient.item(Items.IRON_SWORD);
    @Persisted
    private RecipeIngredient addition = RecipeIngredient.item(Items.AMETHYST_SHARD);
    @Persisted
    private ItemStack result = new ItemStack(Items.DIAMOND_SWORD);

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return CataclysmRecipeFactory.compileWeaponFusion(this);
    }
}
