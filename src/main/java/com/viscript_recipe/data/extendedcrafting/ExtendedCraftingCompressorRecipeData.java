package com.viscript_recipe.data.extendedcrafting;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.extendedcrafting.ExtendedCraftingRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ExtendedCraftingCompressorRecipeData implements IVSRecipeData {
    @Persisted
    private List<ExtendedCraftingCountedIngredientData> inputs = new ArrayList<>(List.of(
            new ExtendedCraftingCountedIngredientData().setIngredient(RecipeIngredient.item(Items.COBBLESTONE)).setCount(64)
    ));
    @Persisted
    private RecipeIngredient catalyst = RecipeIngredient.item(Items.NETHER_STAR);
    @Persisted
    private ItemStack result = new ItemStack(Items.DIAMOND);
    @Persisted
    private int powerCost = 100000;
    @Persisted
    private int powerRate = 500;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ExtendedCraftingRecipeFactory.compileCompressor(this);
    }
}
