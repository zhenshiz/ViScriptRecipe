package com.viscript_recipe.data.ars_nouveau;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.ars_nouveau.ArsNouveauRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class ArsNouveauArmorUpgradeRecipeData implements IVSRecipeData {
    @Persisted
    private List<RecipeIngredient> pedestalItems = new ArrayList<>(List.of(
            RecipeIngredient.item(Items.BLAZE_ROD),
            RecipeIngredient.item(Items.BLAZE_ROD)
    ));
    @Persisted
    private int sourceCost = 2500;
    @Persisted
    private int tier = 1;

    @Override
    public ItemStack getResult() {return new ItemStack(itemFromRegistry("ars_nouveau:arcanist_robes", Items.LEATHER_CHESTPLATE));}

    @Override
    public Recipe<?> compile(ResourceLocation type) {
        return ArsNouveauRecipeFactory.compileArmorUpgrade(this);
    }

    private static Item itemFromRegistry(String id, Item fallback) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) return fallback;
        var item = BuiltInRegistries.ITEM.get(location);
        return item == Items.AIR ? fallback : item;
    }
}
