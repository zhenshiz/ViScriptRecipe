package com.viscript_recipe.compat.extendedcrafting.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.extendedcrafting.ExtendedCraftingRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
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
public class ExtendedCraftingUltimateSingularityRecipeData implements IVSRecipeData {
    @Persisted
    private ItemStack result = new ItemStack(Items.NETHER_STAR);

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ExtendedCraftingRecipeFactory.compileUltimateSingularity(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setResult(new ItemStack(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry
                ("extendedcrafting:ultimate_singularity", Items.NETHER_STAR)));
    }
}
