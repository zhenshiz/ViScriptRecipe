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

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureEnchanterRecipeData implements IVSRecipeData {
    public static final int MAX_INGREDIENTS = 2;

    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @Persisted
    private ResourceLocation enchantment = ResourceLocation.withDefaultNamespace("sharpness");

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return MysticalAgricultureRecipeFactory.compileEnchanter(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setIngredients(new ArrayList<>(List.of(
                RecipeIngredient.item(itemFromRegistry("mysticalagriculture:experience_droplet", Items.EXPERIENCE_BOTTLE)).setCount(8),
                RecipeIngredient.item(itemFromRegistry("mysticalagriculture:prosperity_shard", Items.LAPIS_LAZULI)).setCount(2)
        )));
    }
}
