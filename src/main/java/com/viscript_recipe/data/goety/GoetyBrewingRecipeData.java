package com.viscript_recipe.data.goety;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.compat.goety.GoetyRecipeFactory;
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
public class GoetyBrewingRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.goety.brewing.ingredient", subConfigurable = true)
    private RecipeIngredient ingredient = RecipeIngredient.item(Items.SPIDER_EYE);

    @Configurable(name = "viscript_recipe.config.goety.brewing.effect")
    private ResourceLocation effect = ResourceLocation.withDefaultNamespace("poison");

    @Configurable(name = "viscript_recipe.config.goety.soul_cost")
    private int soulCost = 25;

    @Configurable(name = "viscript_recipe.config.goety.brewing.capacity_extra")
    private int capacityExtra = 1;

    @Configurable(name = "viscript_recipe.config.goety.duration")
    private int duration = 600;

    @Configurable(name = "viscript_recipe.config.goety.brewing.entity_kind")
    private GoetyBrewingEntityKind entityKind = GoetyBrewingEntityKind.NONE;

    @Configurable(name = "viscript_recipe.config.goety.brewing.entity")
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

    /**
     * Compiles this data into Goety's native brewing recipe.
     *
     * @return the compiled brewing recipe
     */
    public Recipe<?> compile() {
        return GoetyRecipeFactory.compileBrewing(this);
    }
}
