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
public class SporeSurgeryRecipeData implements IVSRecipeData {
    public static final int INPUT_COUNT = 16;

    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.IRON_SWORD);

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        return SporeRecipeFactory.compileSurgery(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setResult(new ItemStack(itemFromRegistry("spore:knife", Items.IRON_SWORD)))
                .setIngredient(2, RecipeIngredient.item(itemFromRegistry("spore:mutated_fiber", Items.STRING)));
    }
}
