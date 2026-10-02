package com.viscript_recipe.compat.enderio.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Getter
@Setter
@Accessors(chain = true)
public class EnderIoOutputData implements ISkipDefaultedSerialize {
    @Persisted private ItemStack item = new ItemStack(Items.IRON_INGOT);
    @Persisted private boolean useTag;
    @Persisted private ResourceLocation tag = ResourceLocation.parse("c:ingots/iron");
    @Persisted private int count = 1;
    @Persisted private float chance = 1;
    @Persisted private boolean optional;
    @Persisted private int minCount = 1;
    @Persisted private int maxCount = 1;
}
