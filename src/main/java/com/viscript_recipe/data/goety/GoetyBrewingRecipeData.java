package com.viscript_recipe.data.goety;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.goety.GoetyRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Stores an editable Goety witch cauldron catalyst and its resulting effect metadata.
 */
@Getter
@Setter
@Accessors(chain = true)
public class GoetyBrewingRecipeData implements IVSRecipeData {
    @Persisted
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.SPIDER_EYE);
    @Persisted
    private ResourceLocation effect = ResourceLocation.withDefaultNamespace("poison");
    @Persisted
    private int soulCost = 25;
    @Persisted
    private int capacityExtra = 1;
    @Persisted
    private int duration = 600;
    @Persisted
    private GoetyBrewingEntityKind entityKind = GoetyBrewingEntityKind.NONE;
    @Persisted
    private ResourceLocation entity = ResourceLocation.withDefaultNamespace("zombie");

    /**
     * Returns Goety's derived brew bottle for the non-editable output preview.
     *
     * @return a Goety brew item when registered, otherwise a potion fallback
     */
    public ItemStack visibleResult() {
        var brew = BuiltInRegistries.ITEM.getOptional(ResourceLocation.fromNamespaceAndPath("goety", "brew")).orElse(Items.POTION);
        return new ItemStack(brew);
    }

    @Override
    public ItemStack getResult() {return visibleResult();}

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return GoetyRecipeFactory.compileBrewing(this);
    }
}
