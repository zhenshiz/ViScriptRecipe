package com.viscript_recipe.data.industrial_foregoing;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.viscript_recipe.compat.industrial_foregoing.IndustrialForegoingRecipeFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

/** Editable representation of {@code StoneWorkGenerateRecipe.CODEC}. */
@Getter
@Setter
@Accessors(chain = true)
public class IndustrialStoneWorkRecipeData implements IPersistedSerializable, IConfigurable {
    @Configurable(name = "viscript_recipe.config.recipe.result")
    private ItemStack output = new ItemStack(Items.COBBLESTONE);

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.stonework.water_need")
    private int waterNeed = 1000;

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.stonework.lava_need")
    private int lavaNeed = 1000;

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.stonework.water_consume")
    private int waterConsume;

    @Configurable(name = "viscript_recipe.config.industrial_foregoing.stonework.lava_consume")
    private int lavaConsume;

    /** Compiles the editor data into Industrial Foregoing's native recipe. */
    public Recipe<?> compile() {
        return IndustrialForegoingRecipeFactory.compileStoneWork(this);
    }
}
