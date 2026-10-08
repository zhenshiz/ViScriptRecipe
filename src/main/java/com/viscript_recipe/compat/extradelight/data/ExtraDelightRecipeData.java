package com.viscript_recipe.compat.extradelight.data;

import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.data.IVSRecipeData;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Ingredient;
import java.util.ArrayList;
import java.util.List;

/** 使用现有材料槽位编辑，未主动替换的复杂原料保留完整的原生匹配条件。 */
@Getter
@Setter
public abstract class ExtraDelightRecipeData implements IVSRecipeData {
    @Persisted private List<String> fluidConditions = new ArrayList<>();
    @Persisted private List<String> ingredientConditions = new ArrayList<>();

    @Override
    public <T extends IVSRecipeData> T setIngredient(int index, com.viscript_recipe.data.RecipeIngredient ingredient) {
        if (index >= 0 && index < ingredientConditions.size()) ingredientConditions.set(index, "");
        return IVSRecipeData.super.setIngredient(index, ingredient);
    }

    /**
     * 编译指定槽位的原料，未被替换的复杂条件优先使用原生 Codec 还原。
     *
     * @param index 原料槽位索引
     * @return 原生原料匹配条件
     */
    public Ingredient compileIngredient(int index) {
        String raw = index < ingredientConditions.size() ? ingredientConditions.get(index) : "";
        return raw.isBlank() ? ingredient(index).compile() : decodeIngredient(raw);
    }

    /**
     * 编译工作站原料列表并检查槽位容量。
     *
     * @param max 工作站允许的最大原料数量
     * @param allowEmpty 是否允许只有流体输入的配方
     * @return 原生原料列表
     */
    public NonNullList<Ingredient> compileIngredients(int max, boolean allowEmpty) {
        var values = NonNullList.<Ingredient>create();
        for (int i = 0; i < getIngredients().size(); i++) {
            var ingredient = compileIngredient(i);
            if (!ingredient.isEmpty()) values.add(ingredient);
        }
        if (values.size() > max || !allowEmpty && values.isEmpty())
            throw new IllegalArgumentException("Invalid Extra Delight ingredient count: " + values.size());
        return values;
    }

    /**
     * 保留流体原料的匹配条件，数量使用编辑器当前值。
     *
     * @param index 流体输入槽位索引
     * @param value 编辑器中的流体及数量
     * @return 原生流体原料
     */
    public net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient compileFluid(int index, com.viscript_recipe.data.FluidIngredientData value) {
        String raw = index < fluidConditions.size() ? fluidConditions.get(index) : "";
        if (raw.isBlank()) return com.viscript_recipe.compat.extradelight.ExtraDelightRecipeSupport.sizedFluid(value);
        var ingredient = net.neoforged.neoforge.fluids.crafting.FluidIngredient.CODEC.parse(
                RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry()), JsonParser.parseString(raw)).getOrThrow();
        return new net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient(ingredient, value.getAmount());
    }

    /**
     * 使用当前注册表还原保存的复杂原料。
     *
     * @param raw 原生原料的 JSON 表达
     * @return 原生原料匹配条件
     */
    public static Ingredient decodeIngredient(String raw) {
        return Ingredient.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry()),
                JsonParser.parseString(raw)).getOrThrow();
    }
}
