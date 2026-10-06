package com.viscript_recipe.compat.tacz.data;

import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_recipe.compat.tacz.TaczRecipeFactory;
import com.viscript_recipe.data.IVSRecipeData;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;
import java.util.List;

/** 枪械工作台配方；产物完整保留枪械、弹药或配件的物品组件。 */
@Getter
@Setter
@Accessors(chain = true)
public final class TaczRecipeData implements IVSRecipeData {
    // 与 VSR 通用画布的材料容量保持一致，超过容量的导入必须明确失败。
    public static final int MAX_INPUTS = 1024;
    @Persisted private List<TaczIngredientData> materials = new ArrayList<>(List.of(
            new TaczIngredientData().setValue(RecipeIngredient.item(Items.IRON_INGOT))));
    @Persisted private ItemStack result = new ItemStack(Items.GUNPOWDER);
    @Persisted private ResourceLocation group = ResourceLocation.fromNamespaceAndPath("tacz", "misc");
    @Persisted private int inputSlots = 6;

    @Override
    public Recipe<?> compile(ResourceLocation typeId) { return TaczRecipeFactory.compile(this); }
}
