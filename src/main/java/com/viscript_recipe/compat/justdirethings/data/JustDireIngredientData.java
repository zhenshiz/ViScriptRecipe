package com.viscript_recipe.compat.justdirethings.data;

import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.mojang.serialization.JsonOps;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.data.RecipeIngredient;
import com.viscript_recipe.recipe.importer.RecipeImporter;
import com.viscript_recipe.recipe.importer.RecipeImportException;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Ingredient;

/** 普通材料直接编辑；复杂候选和组件条件保留原生 Codec 数据，明确替换后才移除。 */
@Getter
@Setter
@Accessors(chain = true)
public final class JustDireIngredientData implements ISkipDefaultedSerialize {
    @Persisted private RecipeIngredient value = RecipeIngredient.empty();
    @Persisted private String customJson = "";

    /**
     * 转换普通材料，或保留复杂材料的完整序列化条件。
     *
     * @param ingredient 原生原料条件
     * @param provider 注册表查询上下文
     * @return 可持久化的原料数据
     * @throws RecipeImportException 普通原料无法映射时抛出
     */
    public static JustDireIngredientData from(Ingredient ingredient, HolderLookup.Provider provider) throws RecipeImportException {
        var data = new JustDireIngredientData();
        if (ingredient.isEmpty()) return data;
        if (!ingredient.isCustom() && ingredient.getValues().length == 1) data.value = RecipeImporter.importIngredient(ingredient);
        else data.customJson = Ingredient.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, provider), ingredient).getOrThrow().toString();
        return data;
    }

    /**
     * 还原原生材料条件，空材料返回原生空原料。
     *
     * @return 编译后的原料条件
     * @throws IllegalStateException 保存的复杂材料无法解码时抛出
     */
    public Ingredient compile() {
        return customJson.isBlank() ? value.compile() : Ingredient.CODEC.parse(
                RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry()), JsonParser.parseString(customJson)).getOrThrow();
    }
}
