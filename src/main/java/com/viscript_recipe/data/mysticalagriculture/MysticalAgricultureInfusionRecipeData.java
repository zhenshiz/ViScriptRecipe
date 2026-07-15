package com.viscript_recipe.data.mysticalagriculture;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.compat.mysticalagriculture.MysticalAgricultureRecipeFactory;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the fields encoded by a Mystical Agriculture infusion recipe.
 */
@Getter
@Setter
@Accessors(chain = true)
public class MysticalAgricultureInfusionRecipeData implements IPersistedSerializable, IConfigurable {
    public static final int MAX_PEDESTAL_INGREDIENTS = 8;

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.infusion.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.DIAMOND);

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.infusion.ingredients", subConfigurable = true)
    @ConfigList(addDefaultMethod = "createDefaultIngredient")
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.result")
    private ItemStack result = new ItemStack(Items.EMERALD);

    @Configurable(name = "viscript_recipe.config.mysticalagriculture.transfer_components")
    private boolean transferComponents;

    public RecipeIngredient createDefaultIngredient() {
        return RecipeIngredient.item(Items.STONE);
    }

    public RecipeIngredient ingredient(int index) {
        return index >= 0 && index < ingredients.size() ? ingredients.get(index) : new RecipeIngredient();
    }

    public MysticalAgricultureInfusionRecipeData setIngredient(int index, RecipeIngredient ingredient) {
        while (ingredients.size() <= index && ingredients.size() < MAX_PEDESTAL_INGREDIENTS) {
            ingredients.add(new RecipeIngredient());
        }
        if (index >= 0 && index < ingredients.size()) {
            ingredients.set(index, ingredient == null ? new RecipeIngredient() : ingredient);
        }
        return this;
    }

    public Recipe<?> compile() {
        return MysticalAgricultureRecipeFactory.compileInfusion(this);
    }
}
