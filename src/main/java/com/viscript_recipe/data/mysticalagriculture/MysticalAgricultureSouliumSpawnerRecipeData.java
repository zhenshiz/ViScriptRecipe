package com.viscript_recipe.data.mysticalagriculture;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
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

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureSouliumSpawnerRecipeData implements IVSRecipeData {
    @Persisted
    private MysticalAgricultureCountedIngredientData input = new MysticalAgricultureCountedIngredientData()
            .setIngredient(RecipeIngredient.item(Items.ROTTEN_FLESH));
    @Persisted
    private List<MysticalAgricultureWeightedEntityData> entities = new ArrayList<>();

    @Override
    public ItemStack getResult() {return MysticalAgricultureRecipeUiSupport.firstSpawnEgg(getEntities());}

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return MysticalAgricultureRecipeFactory.compileSouliumSpawner(this);
    }
}
