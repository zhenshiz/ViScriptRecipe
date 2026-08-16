package com.viscript_recipe.data.vanilla;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeEditorTypes;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class CookingRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.RAW_IRON);
    @Persisted
    private ItemStack result = new ItemStack(Items.IRON_INGOT);
    @Persisted
    private float experience = 0.7F;
    @Persisted
    private int cookingTime = 200;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) {
        var factory = factories.get(typeId);
        if (factory == null) return null;

        var compiledIngredient = ingredient == null ? Ingredient.EMPTY : ingredient.compile();
        if (compiledIngredient.isEmpty()) {
            throw new IllegalArgumentException("Cooking recipe ingredient cannot be empty");
        }
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Recipe result cannot be empty");
        }
        return factory.create("", CookingBookCategory.MISC, compiledIngredient, result.copy(), Math.clamp(experience, 0, Integer.MAX_VALUE), Math.max(1, cookingTime));
    }

    static final Map<ResourceLocation, AbstractCookingRecipe.Factory<? extends AbstractCookingRecipe>> factories = Map.of(
            RecipeEditorTypes.SMELTING, SmeltingRecipe::new,
            RecipeEditorTypes.BLASTING, BlastingRecipe::new,
            RecipeEditorTypes.SMOKING, SmokingRecipe::new,
            RecipeEditorTypes.CAMPFIRE_COOKING, CampfireCookingRecipe::new
    );

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        if (typeId.equals(RecipeEditorTypes.BLASTING)) setCookingTime(100);
        else if (typeId.equals(RecipeEditorTypes.SMOKING)) setCookingTime(100);
        else if (typeId.equals(RecipeEditorTypes.CAMPFIRE_COOKING)) setCookingTime(600);
    }
}
