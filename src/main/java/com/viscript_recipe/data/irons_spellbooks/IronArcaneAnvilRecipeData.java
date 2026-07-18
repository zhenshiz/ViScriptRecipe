package com.viscript_recipe.data.irons_spellbooks;

import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
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
public class IronArcaneAnvilRecipeData implements IVSRecipeData {
    @Configurable(name = "viscript_recipe.config.irons_spellbooks.arcane_anvil.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.IRON_SWORD);

    @Configurable(name = "viscript_recipe.config.irons_spellbooks.arcane_anvil.material", subConfigurable = true)
    private RecipeIngredient material = RecipeIngredient.item(Items.AMETHYST_SHARD);

    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack result = new ItemStack(Items.DIAMOND_SWORD);

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        throw new UnsupportedOperationException("Iron's Spells Arcane Anvil recipes are handled by ViScriptRecipe's Arcane Anvil menu hook");
    }
}
