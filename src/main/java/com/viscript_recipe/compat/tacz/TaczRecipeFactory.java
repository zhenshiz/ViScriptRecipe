package com.viscript_recipe.compat.tacz;

import com.tacz.guns.crafting.GunSmithTableIngredient;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.crafting.result.GunSmithTableResult;
import com.viscript_recipe.compat.tacz.data.TaczRecipeData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.ArrayList;

/** 构造原生工作台配方，并为 TACZ 未实现的产物编码提供 VSR 同步适配。 */
public final class TaczRecipeFactory {
    private TaczRecipeFactory() {}

    /**
     * 编译工作台材料、产物和所属页签。
     *
     * @param data 编辑器中的配方数据
     * @return 可供 TACZ 工作台使用的原生配方
     * @throws IllegalArgumentException 材料、数量或产物无效时抛出
     */
    public static GunSmithTableRecipe compile(TaczRecipeData data) {
        if (data.getMaterials().size() > TaczRecipeData.MAX_INPUTS) {
            throw new IllegalArgumentException("TACZ recipe exceeds the editor material capacity");
        }
        var inputs = new ArrayList<GunSmithTableIngredient>();
        for (var material : data.getMaterials()) {
            if (material.isEmpty()) continue;
            if (material.getCount() < 1) throw new IllegalArgumentException("TACZ material count must be positive");
            inputs.add(new GunSmithTableIngredient(material.compile(), material.getCount()));
        }
        if (inputs.isEmpty()) throw new IllegalArgumentException("TACZ recipe must contain at least one material");
        if (data.getResult() == null || data.getResult().isEmpty()) {
            throw new IllegalArgumentException("TACZ recipe result cannot be empty");
        }
        if (data.getGroup() == null) throw new IllegalArgumentException("TACZ recipe tab cannot be empty");
        return new GunSmithTableRecipe(new GunSmithTableResult(data.getResult().copy(), data.getGroup()), inputs);
    }

    /**
     * 用原生解码器支持的 custom 产物格式编码增量同步快照。
     * <p>TACZ 的产物 Codec 未实现 encode；此适配只作用于 VSR 快照，不修改全局序列化器。
     *
     * @param recipe 原生工作台配方
     * @param provider 注册表查询上下文
     * @return 可通过原版配方 Codec 解码的配方 NBT
     */
    public static CompoundTag encodeSnapshot(Recipe<?> recipe, HolderLookup.Provider provider) {
        var table = (GunSmithTableRecipe) recipe;
        table.init(provider);
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        var encoded = new CompoundTag();
        encoded.putString("type", TaczRecipeEditorTypes.CRAFTING.toString());
        encoded.put("materials", GunSmithTableIngredient.CODEC.listOf().encodeStart(ops, table.getInputs()).getOrThrow());
        var result = new CompoundTag();
        result.putString("type", GunSmithTableResult.CUSTOM);
        result.put("item", ItemStack.OPTIONAL_CODEC.encodeStart(ops, table.getOutput()).getOrThrow());
        result.putString("group", table.getTab().toString());
        encoded.put("result", result);
        return encoded;
    }
}
