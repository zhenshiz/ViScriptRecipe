package com.viscript_recipe.compat.extendedcrafting.data;

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
            ShapedKeyEntry.of('A', RecipeIngredient.item(Items.ENDER_EYE))
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

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setPattern(new ArrayList<>(List.of(
                " A ",
                "ABA",
                " A "
        ))).setKey(new ArrayList<>(List.of(
                        ShapedKeyEntry.of('A', RecipeIngredient.item(Items.ENDER_EYE)),
                        ShapedKeyEntry.of('B', RecipeIngredient.item(Items.NETHER_STAR))
                )))
                .setShapelessIngredients(new ArrayList<>(List.of(
                        RecipeIngredient.item(Items.ENDER_EYE),
                        RecipeIngredient.item(Items.NETHER_STAR)
                )))
                .setResult(new ItemStack(com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry("extendedcrafting:ender_star", Items.ENDER_EYE)))
                .setCraftingTime(typeId.equals(ExtendedCraftingRecipeEditorTypes.SHAPELESS_ENDER_CRAFTER) ? 100 : 200);
    }
}
