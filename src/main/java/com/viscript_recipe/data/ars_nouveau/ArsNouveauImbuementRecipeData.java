package com.viscript_recipe.data.ars_nouveau;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.ars_nouveau.ArsNouveauRecipeFactory;
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
public class ArsNouveauImbuementRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.LAPIS_LAZULI);
    @Persisted
    private List<RecipeIngredient> pedestalItems = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.AMETHYST_SHARD);
    @Persisted
    private int source = 500;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ArsNouveauRecipeFactory.compileImbuement(this);
    }
}
