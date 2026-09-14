package com.viscript_recipe.compat.kaleidoscope_cookery.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.kaleidoscope_cookery.KaleidoscopeCookeryRecipeFactory;
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
public class KaleidoscopePotRecipeData implements IVSRecipeData {
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.EGG),
            RecipeIngredient.item(Items.CARROT),
            RecipeIngredient.item(Items.POTATO)
    ));
    @Persisted
    private ItemStack result = new ItemStack(Items.BAKED_POTATO);
    @Persisted
    private RecipeIngredient carrier = RecipeIngredient.item(Items.BOWL);
    @Persisted
    private int time = 200;
    @Persisted
    private int stirFryCount = 3;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return KaleidoscopeCookeryRecipeFactory.compilePot(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setIngredients(new ArrayList<>(List.of(
                RecipeIngredient.item(Items.EGG),
                RecipeIngredient.item(Items.EGG),
                RecipeIngredient.item(itemFromRegistry("kaleidoscope_cookery:tomato", Items.CARROT))
        ))).setResult(new ItemStack(itemFromRegistry("kaleidoscope_cookery:scramble_egg_with_tomatoes", Items.BAKED_POTATO)));
    }
}
