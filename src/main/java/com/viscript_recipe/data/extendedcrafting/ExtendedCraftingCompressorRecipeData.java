package com.viscript_recipe.data.extendedcrafting;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
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
    @Configurable(name = "viscript_recipe.config.extendedcrafting.compressor.inputs")
    @ConfigList(addDefaultMethod = "createDefaultInput")
    private List<ExtendedCraftingCountedIngredientData> inputs = new ArrayList<>(List.of(
            new ExtendedCraftingCountedIngredientData().setIngredient(RecipeIngredient.item(Items.COBBLESTONE)).setCount(64)
    ));

    @Configurable(name = "viscript_recipe.config.extendedcrafting.compressor.catalyst", subConfigurable = true)
    private RecipeIngredient catalyst = RecipeIngredient.item(Items.NETHER_STAR);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.DIAMOND);

    @Configurable(name = "viscript_recipe.config.extendedcrafting.power_cost")
    private int powerCost = 100000;

    @Configurable(name = "viscript_recipe.config.extendedcrafting.power_rate")
    private int powerRate = 500;

    public ExtendedCraftingCountedIngredientData createDefaultInput() {
        return new ExtendedCraftingCountedIngredientData();
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ExtendedCraftingRecipeFactory.compileCompressor(this);
    }
}
