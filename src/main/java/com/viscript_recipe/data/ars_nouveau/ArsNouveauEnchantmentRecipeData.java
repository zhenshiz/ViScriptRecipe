package com.viscript_recipe.data.ars_nouveau;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
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
public class ArsNouveauEnchantmentRecipeData implements IVSRecipeData {
    @Configurable(name = "viscript_recipe.config.ars_nouveau.pedestal_items")
    @ConfigList(addDefaultMethod = "createDefaultPedestalItem")
    private List<RecipeIngredient> pedestalItems = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.LAPIS_BLOCK),
            RecipeIngredient.item(Items.AMETHYST_SHARD)
    ));

    @Configurable(name = "viscript_recipe.config.ars_nouveau.enchantment.enchantment")
    private ResourceLocation enchantment = ResourceLocation.withDefaultNamespace("sharpness");

    @Configurable(name = "viscript_recipe.config.ars_nouveau.enchantment.level")
    private int level = 1;

    @Configurable(name = "viscript_recipe.config.ars_nouveau.source_cost")
    private int sourceCost = 1000;

    public RecipeIngredient createDefaultPedestalItem() {
        return RecipeIngredient.item(Items.AMETHYST_SHARD);
    }

    @Override
    public ItemStack getResult() {return new ItemStack(Items.ENCHANTED_BOOK);}

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ArsNouveauRecipeFactory.compileEnchantment(this);
    }
}
