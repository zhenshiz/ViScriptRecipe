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
public class CataclysmWeaponFusionRecipeData implements IVSRecipeData {
    @Configurable(name = "viscript_recipe.config.cataclysm.weapon_fusion.base", subConfigurable = true)
    private RecipeIngredient base = RecipeIngredient.item(Items.IRON_SWORD);

    @Configurable(name = "viscript_recipe.config.cataclysm.weapon_fusion.addition", subConfigurable = true)
    private RecipeIngredient addition = RecipeIngredient.item(Items.AMETHYST_SHARD);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.DIAMOND_SWORD);

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return CataclysmRecipeFactory.compileWeaponFusion(this);
    }
}
