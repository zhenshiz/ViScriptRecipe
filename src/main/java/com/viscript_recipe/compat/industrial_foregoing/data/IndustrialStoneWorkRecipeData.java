package com.viscript_recipe.compat.industrial_foregoing.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
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
    @Persisted
    private ItemStack output = new ItemStack(Items.COBBLESTONE);
    @Persisted
    private int waterNeed = 1000;
    @Persisted
    private int lavaNeed = 1000;
    @Persisted
    private int waterConsume;
    @Persisted
    private int lavaConsume;

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return IndustrialForegoingRecipeFactory.compileStoneWork(this);
    }
}
