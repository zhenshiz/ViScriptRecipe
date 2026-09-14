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

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureSouliumSpawnerRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.ROTTEN_FLESH);
    @Persisted
    private List<MysticalAgricultureWeightedEntityData> entities = new ArrayList<>();

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return MysticalAgricultureRecipeFactory.compileSouliumSpawner(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setInput(RecipeIngredient.item(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry
                ("mysticalagriculture:soulium_ingot", Items.ROTTEN_FLESH)).setCount(4))
                .setEntities(new ArrayList<>(List.of(new MysticalAgricultureWeightedEntityData())));
    }
}
