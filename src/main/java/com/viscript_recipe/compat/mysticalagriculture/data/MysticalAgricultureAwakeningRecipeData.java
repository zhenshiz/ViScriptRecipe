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
public class MysticalAgricultureAwakeningRecipeData implements IVSRecipeData {
    public static final int PEDESTAL_INGREDIENT_COUNT = 4;
    public static final int ESSENCE_COUNT = 4;

    @Persisted
    private RecipeIngredient input = RecipeIngredient.item(Items.NETHER_STAR);
    @Persisted
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @Persisted
    private List<ItemStack> essences = new ArrayList<>();
    @Persisted
    private ItemStack result = new ItemStack(Items.NETHER_STAR);
    @Persisted
    private boolean transferComponents;

    public ItemStack essence(int index) {
        return index >= 0 && index < essences.size() && essences.get(index) != null ? essences.get(index) : ItemStack.EMPTY;
    }

    public MysticalAgricultureAwakeningRecipeData setEssence(int index, ItemStack essence) {
        while (essences.size() <= index && essences.size() < ESSENCE_COUNT) {
            essences.add(ItemStack.EMPTY);
        }
        if (index >= 0 && index < essences.size()) {
            essences.set(index, essence == null ? ItemStack.EMPTY : essence.copy());
        }
        return this;
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return MysticalAgricultureRecipeFactory.compileAwakening(this);
    }

    @Override
    public void applyDefaultData(ResourceLocation typeId) {
        setInput(RecipeIngredient.item(itemFromRegistry("mysticalagriculture:supremium_essence", Items.NETHER_STAR)))
                .setIngredients(new ArrayList<>(List.of(
                        RecipeIngredient.item(itemFromRegistry("mysticalagriculture:prosperity_gemstone", Items.EMERALD)),
                        RecipeIngredient.item(Items.NETHER_STAR),
                        RecipeIngredient.item(Items.DRAGON_BREATH),
                        RecipeIngredient.item(Items.END_CRYSTAL)
                )))
                .setResult(new ItemStack(itemFromRegistry("mysticalagriculture:awakened_supremium_essence", Items.NETHER_STAR)));
        setEssences(new ArrayList<>(List.of(
                new ItemStack(itemFromRegistry("mysticalagriculture:air_essence", Items.FEATHER)),
                new ItemStack(itemFromRegistry("mysticalagriculture:earth_essence", Items.DIRT)),
                new ItemStack(itemFromRegistry("mysticalagriculture:water_essence", Items.WATER_BUCKET)),
                new ItemStack(itemFromRegistry("mysticalagriculture:fire_essence", Items.BLAZE_POWDER))
        )));
    }
}
