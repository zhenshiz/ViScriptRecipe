package com.viscript_recipe.compat.cataclysm.data;

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

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

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

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setIngredient(RecipeIngredient.item(itemFromRegistry("cataclysm:amethyst_crab_meat", Items.COOKED_COD)))
                .setResult(new ItemStack(itemFromRegistry("cataclysm:blessed_amethyst_crab_meat", Items.GOLDEN_CARROT)))
                .setTime(120);
    }
}
