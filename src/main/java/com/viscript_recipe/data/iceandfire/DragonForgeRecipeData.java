package com.viscript_recipe.data.iceandfire;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.iceandfire.IceAndFireRecipeFactory;
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
public class DragonForgeRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.IRON_INGOT);
    @Persisted
    private RecipeIngredient blood = RecipeIngredient.item(Items.GLASS_BOTTLE);
    @Persisted
    private ItemStack result = new ItemStack(Items.IRON_INGOT);
    @Persisted
    private String dragonType = "fire";
    @Persisted
    private int cookTime = 1000;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return IceAndFireRecipeFactory.compileDragonForge(this);
    }
}
