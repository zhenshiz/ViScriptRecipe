package com.viscript_recipe.data.mysticalagriculture;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeFactory;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Stores the input and nested soul result encoded by a soul extraction recipe.
 */
@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureSoulExtractionRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.mysticalagriculture.soul_extraction.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.ROTTEN_FLESH);

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.soul_extraction.soul_type")
    private ResourceLocation soulType = ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "zombie");

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.soul_extraction.souls")
    private double souls = 1.0D;

    public Recipe<?> compile() {
        return MysticalAgricultureRecipeFactory.compileSoulExtraction(this);
    }
}
