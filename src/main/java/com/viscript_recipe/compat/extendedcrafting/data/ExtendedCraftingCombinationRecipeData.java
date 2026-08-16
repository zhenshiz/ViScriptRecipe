package com.viscript_recipe.compat.extendedcrafting.data;

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
public class ExtendedCraftingCombinationRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.DIAMOND);
    @Persisted
    private List<RecipeIngredient> pedestalItems = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.GOLD_INGOT)
    ));
    @Persisted
    private ItemStack result = new ItemStack(Items.NETHER_STAR);
    @Persisted
    private int powerCost = 100000;
    @Persisted
    private int powerRate = 500;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ExtendedCraftingRecipeFactory.compileCombination(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setPedestalItems(new ArrayList<>(List.of(
                        RecipeIngredient.item(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry
                                ("extendedcrafting:black_iron_ingot", Items.IRON_INGOT)),
                        RecipeIngredient.item(Items.GOLD_INGOT),
                        RecipeIngredient.item(Items.REDSTONE)
                )));
    }
}
