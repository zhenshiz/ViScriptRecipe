package com.viscript_recipe.compat.enderio.data;

import com.google.gson.JsonParser;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.mojang.serialization.JsonOps;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class EnderIoIngredientData implements ISkipDefaultedSerialize {
    @Persisted private List<RecipeIngredient> alternatives = new ArrayList<>();
    @Persisted private int count = 1;
    // 特殊原料保留完整编解码数据，避免展开预览物品后丢失灵魂或组件条件。
    @Persisted private String customJson = "";

    public static EnderIoIngredientData of(RecipeIngredient ingredient) {
        var data = new EnderIoIngredientData();
        if (!ingredient.isEmpty()) data.alternatives.add(ingredient);
        return data;
    }

    public static EnderIoIngredientData from(Ingredient ingredient, HolderLookup.Provider provider) {
        var data = new EnderIoIngredientData();
        if (ingredient.isEmpty()) return data;
        if (ingredient.isCustom()) {
            data.customJson = Ingredient.CODEC.encodeStart(RegistryOps.create(JsonOps.INSTANCE, provider), ingredient).getOrThrow().toString();
            return data;
        }
        for (var value : ingredient.getValues()) {
            if (value instanceof Ingredient.ItemValue item) data.alternatives.add(RecipeIngredient.item(item.item().copy()));
            else if (value instanceof Ingredient.TagValue tag) data.alternatives.add(RecipeIngredient.tag(tag.tag().location()));
            else throw new IllegalArgumentException("不支持的原料值：" + value);
        }
        return data;
    }

    public Ingredient compile() {
        if (!customJson.isBlank()) return Ingredient.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, Platform.getFrozenRegistry()),
                JsonParser.parseString(customJson)).getOrThrow();
        var children = alternatives.stream().filter(i -> !i.isEmpty()).map(RecipeIngredient::compile).toList();
        if (children.isEmpty()) return Ingredient.EMPTY;
        if (children.size() == 1) return children.getFirst();
        if (children.stream().anyMatch(Ingredient::isCustom)) return CompoundIngredient.of(children.toArray(Ingredient[]::new));
        return Ingredient.fromValues(children.stream().flatMap(i -> java.util.Arrays.stream(i.getValues())));
    }
}
