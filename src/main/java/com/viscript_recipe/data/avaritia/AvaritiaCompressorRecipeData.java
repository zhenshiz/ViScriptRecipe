package com.viscript_recipe.data.avaritia;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.avaritia.AvaritiaRecipeFactory;
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
public class AvaritiaCompressorRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.COBBLESTONE);
    @Persisted
    private ItemStack result = new ItemStack(Items.BEDROCK);
    @Persisted
    private int inputCount = 1000;
    @Persisted
    private int timeCost = 240;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return AvaritiaRecipeFactory.compileCompressor(this);
    }
}
