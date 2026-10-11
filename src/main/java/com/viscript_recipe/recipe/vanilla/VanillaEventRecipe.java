package com.viscript_recipe.recipe.vanilla;

import com.viscript_recipe.data.vanilla.VanillaEventRecipeData;
import com.viscript_recipe.data.vanilla.VanillaEventRecipeEditorTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class VanillaEventRecipe implements Recipe<VanillaEventRecipe.Input> {
    private final ResourceLocation editorType;
    private final VanillaEventRecipeData data;
    private final Ingredient input;
    private final Ingredient material;

    public VanillaEventRecipe(ResourceLocation editorType, VanillaEventRecipeData data) {
        this.editorType = editorType;
        this.data = data.copy();
        input = this.data.getInput().compile();
        material = this.data.getMaterial().compile();
        if (input.isEmpty() || material.isEmpty() || this.data.getResult().isEmpty()) {
            throw new IllegalArgumentException("Event recipe requires two inputs and a result");
        }
        if (isBrewing()) {
            this.data.setResult(this.data.getResult().copyWithCount(1));
        } else if (this.data.getLevelCost() < 1 || this.data.getMaterialCost() < 1) {
            throw new IllegalArgumentException("Anvil level and material costs must be positive");
        }
    }

    public ResourceLocation editorType() {return editorType;}
    public VanillaEventRecipeData data() {return data.copy();}
    public boolean isBrewing() {return editorType.equals(VanillaEventRecipeEditorTypes.BREWING);}
    public boolean acceptsInput(ItemStack stack) {return input.test(stack);}
    public boolean acceptsMaterial(ItemStack stack) {return material.test(stack);}
    public int levelCost() {return data.getLevelCost();}
    public int materialCost() {return data.getMaterialCost();}
    public ItemStack result() {return data.getResult().copy();}

    public boolean matchesItems(ItemStack left, ItemStack right) {
        return acceptsInput(left) && acceptsMaterial(right);
    }

    @Override
    public boolean matches(Input input, Level level) {
        return matchesItems(input.left(), input.right()) && (isBrewing()
                ? input.left().getCount() == 1 : input.right().getCount() >= materialCost());
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider provider) {return result();}

    @Override
    public boolean canCraftInDimensions(int width, int height) {return false;}

    @Override
    public boolean isSpecial() {return true;}

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {return result();}

    @Override
    public NonNullList<Ingredient> getIngredients() {return NonNullList.of(Ingredient.EMPTY, input, material);}

    @Override
    public RecipeSerializer<?> getSerializer() {
        return isBrewing() ? VanillaEventRecipeRuntime.BREWING_SERIALIZER.get() : VanillaEventRecipeRuntime.ANVIL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return isBrewing() ? VanillaEventRecipeRuntime.BREWING_TYPE.get() : VanillaEventRecipeRuntime.ANVIL_TYPE.get();
    }

    public record Input(ItemStack left, ItemStack right) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {return switch (index) {case 0 -> left; case 1 -> right; default -> throw new IndexOutOfBoundsException(index);};}
        @Override
        public int size() {return 2;}
    }
}
