package com.viscript_recipe.data.mysticalagriculture;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeFactory;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeUiSupport;
import com.viscript_recipe.data.IVSRecipeData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureEnchanterRecipeData implements IVSRecipeData {
    public static final int MAX_INGREDIENTS = 2;

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.enchanter.ingredients", subConfigurable = true)
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<MysticalAgricultureCountedIngredientData> ingredients = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.enchanter.enchantment")
    private ResourceLocation enchantment = ResourceLocation.withDefaultNamespace("sharpness");

    public MysticalAgricultureCountedIngredientData createDefaultIngredient() {
        return new MysticalAgricultureCountedIngredientData();
    }

    public MysticalAgricultureCountedIngredientData ingredient(int index) {
        return index >= 0 && index < ingredients.size()
                ? ingredients.get(index)
                : new MysticalAgricultureCountedIngredientData();
    }

    public MysticalAgricultureEnchanterRecipeData setIngredient(
            int index,
            MysticalAgricultureCountedIngredientData ingredient
    ) {
        while (ingredients.size() <= index && ingredients.size() < MAX_INGREDIENTS) {
            ingredients.add(new MysticalAgricultureCountedIngredientData());
        }
        if (index >= 0 && index < ingredients.size()) {
            ingredients.set(index, ingredient == null ? new MysticalAgricultureCountedIngredientData() : ingredient);
        }
        return this;
    }

    @Override
    public ItemStack getResult() {
        return MysticalAgricultureRecipeUiSupport.firstEnchantedBook(getEnchantment());
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return MysticalAgricultureRecipeFactory.compileEnchanter(this);
    }
}
