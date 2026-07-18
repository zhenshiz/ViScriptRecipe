package com.viscript_recipe.data.mysticalagriculture;

import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeFactory;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeUiSupport;
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
public class MysticalAgricultureSoulExtractionRecipeData implements IVSRecipeData {
    @Configurable(name = "viscript_recipe.config.mysticalagriculture.soul_extraction.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.ROTTEN_FLESH);

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.soul_extraction.soul_type")
    private ResourceLocation soulType = ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "zombie");

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.soul_extraction.souls")
    private double souls = 1.0D;

    @Override
    public ItemStack getResult() {return MysticalAgricultureRecipeUiSupport.soulJar(this);}

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return MysticalAgricultureRecipeFactory.compileSoulExtraction(this);
    }
}
