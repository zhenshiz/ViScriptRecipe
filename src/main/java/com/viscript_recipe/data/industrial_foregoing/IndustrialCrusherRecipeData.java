package com.viscript_recipe.data.industrial_foregoing;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.compat.industrial_foregoing.IndustrialForegoingRecipeFactory;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

/** Editable representation of {@code CrusherRecipe.CODEC}. */
@Getter
@Setter
@Accessors(chain = true)
public class IndustrialCrusherRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.industrial_foregoing.crusher.input", subConfigurable = true)
    private RecipeIngredient input = RecipeIngredient.item(Items.COBBLESTONE);

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.crusher.output", subConfigurable = true)
    private RecipeIngredient output = RecipeIngredient.item(Items.GRAVEL);

    /** Compiles the editor data into Industrial Foregoing's native recipe. */
    public Recipe<?> compile() {
        return IndustrialForegoingRecipeFactory.compileCrusher(this);
    }
}
