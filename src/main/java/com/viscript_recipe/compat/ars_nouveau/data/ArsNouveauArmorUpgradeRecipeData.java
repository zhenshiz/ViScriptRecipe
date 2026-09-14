package com.viscript_recipe.compat.ars_nouveau.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.ars_nouveau.ArsNouveauRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.RecipeHelper;
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
public class ArsNouveauArmorUpgradeRecipeData implements IVSRecipeData, IPreview {
    @Persisted
    private List<RecipeIngredient> pedestalItems = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.BLAZE_ROD),
            RecipeIngredient.item(Items.BLAZE_ROD)
    ));
    @Persisted
    private int sourceCost = 2500;
    @Persisted
    private int tier = 1;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ArsNouveauRecipeFactory.compileArmorUpgrade(this);
    }

    @Override
    public ItemStack centerPreview() {
        return RecipeHelper.registryItem("ars_nouveau:arcanist_robes", Items.LEATHER_CHESTPLATE);
    }

    @Override
    public ItemStack outputPreview() {
        return RecipeHelper.registryItem("ars_nouveau:arcanist_robes", Items.LEATHER_CHESTPLATE);
    }
}
