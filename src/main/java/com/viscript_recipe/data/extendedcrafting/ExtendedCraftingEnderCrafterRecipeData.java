package com.viscript_recipe.data.extendedcrafting;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.extendedcrafting.ExtendedCraftingRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.data.vanilla.ShapedKeyEntry;
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
public class ExtendedCraftingEnderCrafterRecipeData implements IVSRecipeData {
    @Persisted
    private List<String> pattern = new ArrayList<>(List.of("A"));
    @Persisted
    private List<ShapedKeyEntry> key = new ArrayList<>(List.of(
            ShapedKeyEntry.of("A", RecipeIngredient.item(Items.ENDER_EYE))
    ));
    @Persisted
    private List<RecipeIngredient> shapelessIngredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.ENDER_EYE)
    ));
    @Persisted
    private ItemStack result = new ItemStack(Items.ENDER_EYE);
    @Persisted
    private int craftingTime = 200;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ExtendedCraftingRecipeFactory.compileEnderCrafter(type, this);
    }
}
