package com.viscript_recipe.compat.mysticalagriculture.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureSoulExtractionRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.ROTTEN_FLESH);
    @Persisted
    private ResourceLocation soulType = ResourceLocation.fromNamespaceAndPath("mysticalagriculture", "zombie");
    @Persisted
    private double souls = 1.0D;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return MysticalAgricultureRecipeFactory.compileSoulExtraction(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setInput(RecipeIngredient.item(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry
                ("mysticalagriculture:corrupted_essence", Items.ROTTEN_FLESH)));
    }
}
