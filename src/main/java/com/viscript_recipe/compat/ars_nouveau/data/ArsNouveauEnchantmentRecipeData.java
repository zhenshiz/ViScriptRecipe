package com.viscript_recipe.compat.ars_nouveau.data;

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
public class ArsNouveauEnchantmentRecipeData implements IVSRecipeData, IPreview {
    @Persisted
    private List<RecipeIngredient> pedestalItems = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.LAPIS_BLOCK),
            RecipeIngredient.item(Items.AMETHYST_SHARD)
    ));
    @Persisted
    private ResourceLocation enchantment = ResourceLocation.withDefaultNamespace("sharpness");
    @Persisted
    private int level = 1;
    @Persisted
    private int sourceCost = 1000;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ArsNouveauRecipeFactory.compileEnchantment(this);
    }

    @Override
    public ItemStack centerPreview() {return new ItemStack(level <= 1 ? Items.BOOK : Items.ENCHANTED_BOOK);}

    @Override
    public ItemStack outputPreview() {return new ItemStack(Items.ENCHANTED_BOOK);}
}
