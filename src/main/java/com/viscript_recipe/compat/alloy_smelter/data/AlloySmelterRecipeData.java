package com.viscript_recipe.compat.alloy_smelter.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.alloy_smelter.AlloySmelterRecipeFactory;
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
public class AlloySmelterRecipeData implements IVSRecipeData {
    public static final int MAX_INPUTS = 5;

    @Persisted
    private List<RecipeIngredient> materials = new ArrayList<>(List.of(RecipeIngredient.item(Items.RAW_IRON)));
    @Persisted
    private ItemStack result = new ItemStack(Items.IRON_INGOT);
    @Persisted
    private int smeltingTime = 200;
    @Persisted
    private int fuelPerTick = 1;
    @Persisted
    private int requiredTier = 1;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return AlloySmelterRecipeFactory.compile(this);
    }
}
