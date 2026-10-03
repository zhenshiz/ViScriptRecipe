package com.viscript_recipe.compat.ars_nouveau.data;

import net.minecraft.world.item.ItemStack;

public interface IPreview {
    default ItemStack centerPreview() {return ItemStack.EMPTY;}

    default ItemStack outputPreview() {return ItemStack.EMPTY;}
}
