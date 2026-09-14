package com.viscript_recipe.compat.kaleidoscope_tavern.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.kaleidoscope_tavern.KaleidoscopeTavernRecipeFactory;
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
public class KaleidoscopeShakerRecipeData implements IVSRecipeData {
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.POTION);

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return KaleidoscopeTavernRecipeFactory.compileShaker(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setIngredients(new ArrayList<>(List.of(
                RecipeIngredient.item(itemFromRegistry("kaleidoscope_tavern:grape", Items.SWEET_BERRIES))
        ))).setResult(new ItemStack(itemFromRegistry("kaleidoscope_tavern:bloody_mary", Items.POTION)));
    }
}
