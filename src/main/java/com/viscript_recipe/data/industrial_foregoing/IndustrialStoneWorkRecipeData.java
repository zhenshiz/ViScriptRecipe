package com.viscript_recipe.data.industrial_foregoing;

import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.viscript_recipe.compat.industrial_foregoing.IndustrialForegoingRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

@Getter
@Setter
@Accessors(chain = true)
public class IndustrialStoneWorkRecipeData implements IVSRecipeData {
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

    @Override
    public ItemStack getResult() {return getOutput();}

    @Override
    public <T extends IVSRecipeData> T setResult(ItemStack result) {
        setOutput(result);
        //noinspection unchecked
        return (T) this;
    }

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return IndustrialForegoingRecipeFactory.compileStoneWork(this);
    }
}
