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

import static com.viscript_recipe.recipe.RecipeHelper.itemFromRegistry;

@Getter
@Setter
@Accessors(chain = true)
public class ExtendedCraftingTableRecipeData implements IVSRecipeData {
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 9;

    @Persisted
    private int width = 3;
    @Persisted
    private int height = 3;
    @Persisted
    private int tier = 1;
    @Persisted
    private List<String> pattern = new ArrayList<>(List.of("A"));
    @Persisted
    private List<ShapedKeyEntry> key = new ArrayList<>(List.of(
            ShapedKeyEntry.of('A', RecipeIngredient.item(Items.IRON_INGOT))
    ));
    @Persisted
    private List<RecipeIngredient> shapelessIngredients = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.IRON_INGOT)
    ));
    @Persisted
    private ItemStack result = new ItemStack(Items.CRAFTING_TABLE);

    public ExtendedCraftingTableRecipeData setWidth(int width) {
        this.width = clampSize(width);
        return this;
    }

    public ExtendedCraftingTableRecipeData setHeight(int height) {
        this.height = clampSize(height);
        return this;
    }

    public ExtendedCraftingTableRecipeData setTier(int tier) {
        this.tier = Math.clamp(tier, 0, 4);
        return this;
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ExtendedCraftingRecipeFactory.compileTable(type, this);
    }

    private static int clampSize(int value) {
        return Math.clamp(value, MIN_SIZE, MAX_SIZE);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        var tier = ExtendedCraftingRecipeEditorTypes.tableTierForType(typeId);
        var gridSize = ExtendedCraftingRecipeEditorTypes.tableGridSizeForTier(tier);
        setTier(tier).setWidth(gridSize).setHeight(gridSize)
                .setKey(new ArrayList<>(List.of(
                        ShapedKeyEntry.of('A', RecipeIngredient.item(itemFromRegistry("extendedcrafting:black_iron_ingot", Items.IRON_INGOT)))
                )))
                .setShapelessIngredients(new ArrayList<>(List.of(
                        RecipeIngredient.item(itemFromRegistry("extendedcrafting:black_iron_ingot", Items.IRON_INGOT))
                )))
                .setResult(new ItemStack(itemFromRegistry(switch (tier) {
                    case 1 -> "extendedcrafting:basic_component";
                    case 2 -> "extendedcrafting:advanced_component";
                    case 3 -> "extendedcrafting:elite_component";
                    default -> "extendedcrafting:ultimate_component";
                }, Items.CRAFTING_TABLE)));
    }
}
