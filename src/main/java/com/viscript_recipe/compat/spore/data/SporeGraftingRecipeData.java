package com.viscript_recipe.compat.spore.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.spore.SporeRecipeFactory;
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
public class SporeGraftingRecipeData implements IVSRecipeData {
    public static final int INPUT_COUNT = 3;

    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.IRON_HELMET);

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return SporeRecipeFactory.compileGrafting(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setResult(new ItemStack(itemFromRegistry("spore:inf_up_helmet", Items.NETHERITE_HELMET)))
                .setIngredients(new ArrayList<>(List.of(
                        RecipeIngredient.item(itemFromRegistry("spore:inf_helmet", Items.IRON_HELMET)),
                        RecipeIngredient.item(itemFromRegistry("spore:brain_remnants", Items.ROTTEN_FLESH)),
                        RecipeIngredient.item(itemFromRegistry("spore:respirator", Items.LEATHER_HELMET))
                )));
    }
}
