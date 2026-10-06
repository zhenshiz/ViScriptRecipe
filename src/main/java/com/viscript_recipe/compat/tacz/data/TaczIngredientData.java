package com.viscript_recipe.compat.tacz.data;

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

/** 保留工作台材料的独立数量及无法映射为单个物品或标签的原料条件。 */
@Getter
@Setter
@Accessors(chain = true)
public final class TaczIngredientData implements ISkipDefaultedSerialize {
    @Persisted private RecipeIngredient value = RecipeIngredient.empty();
    @Persisted private int count = 1;
    @Persisted private String customJson = "";

    /**
     * 导入原生材料；多候选和特殊原料保留完整 Codec 数据。
     *
     * @param ingredient 原生原料条件
     * @param count 所需数量
     * @param provider 注册表查询上下文
     * @return 可持久化的材料数据
     * @throws RecipeImportException 普通原料无法映射到编辑器时抛出
     */
    public static TaczIngredientData from(Ingredient ingredient, int count, HolderLookup.Provider provider) throws RecipeImportException {
        var data = new TaczIngredientData().setCount(count);
        if (!ingredient.isCustom() && ingredient.getValues().length == 1) {
            data.value = RecipeImporter.importIngredient(ingredient).setCount(1);
        } else if (!ingredient.isEmpty()) {
            data.customJson = Ingredient.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, provider), ingredient)
                    .getOrThrow().toString();
        }
        return data;
    }

    /**
     * 编译材料条件，不把预览物品当作原始条件。
     *
     * @return 原生原料条件
     */
    public Ingredient compile() {
        return customJson.isBlank() ? value.compile() : Ingredient.CODEC.parse(
                RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry()), JsonParser.parseString(customJson)).getOrThrow();
    }

    /**
     * 判断材料槽是否为空。
     *
     * @return 没有普通或特殊原料时为 {@code true}
     */
    public boolean isEmpty() { return customJson.isBlank() && value.isEmpty(); }
}
