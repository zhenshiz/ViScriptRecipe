package com.viscript_recipe.data.cataclysm;

import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
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

@Getter
@Setter
@Accessors(chain = true)
public class CataclysmAmethystBlessRecipeData implements IVSRecipeData {
    @Configurable(name = "viscript_recipe.config.cataclysm.amethyst_bless.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.AMETHYST_SHARD);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.AMETHYST_BLOCK);

    @Configurable(name = "viscript_recipe.config.cataclysm.amethyst_bless.time")
    private int time = 200;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return CataclysmRecipeFactory.compileAmethystBless(this);
    }
}
