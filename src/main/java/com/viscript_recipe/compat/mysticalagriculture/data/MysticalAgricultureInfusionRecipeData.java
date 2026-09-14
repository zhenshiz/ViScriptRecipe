package com.viscript_recipe.compat.mysticalagriculture.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeFactory;
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

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureInfusionRecipeData implements IVSRecipeData {
    public static final int MAX_PEDESTAL_INGREDIENTS = 8;

    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.DIAMOND);
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.EMERALD);
    @Persisted
    private boolean transferComponents;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return MysticalAgricultureRecipeFactory.compileInfusion(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setIngredients(new ArrayList<>(List.of(
                RecipeIngredient.item(itemFromRegistry("mysticalagriculture:inferium_essence", Items.REDSTONE)),
                RecipeIngredient.item(itemFromRegistry("mysticalagriculture:prosperity_shard", Items.AMETHYST_SHARD))
        )))
                .setResult(new ItemStack(itemFromRegistry("mysticalagriculture:prosperity_gemstone", Items.EMERALD)));
    }
}
