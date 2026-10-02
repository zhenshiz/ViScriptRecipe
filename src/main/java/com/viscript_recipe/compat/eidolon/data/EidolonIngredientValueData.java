package com.viscript_recipe.compat.eidolon.data;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.data.IngredientValueKind;
import com.viscript_recipe.data.RecipeIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

/** 显式的数据组件匹配条件与物品堆隐含的默认组件分开保存。 */
@Getter
@Setter
@Accessors(chain = true)
public class EidolonIngredientValueData implements ISkipDefaultedSerialize {
    @Persisted
    private RecipeIngredient value = RecipeIngredient.empty();
    @Persisted
    private boolean matchComponents;
    @Persisted
    private boolean strict;
    @Persisted
    private CompoundTag componentPredicate = new CompoundTag();

    public static EidolonIngredientValueData of(RecipeIngredient value) {
        return new EidolonIngredientValueData().setValue(value.copy());
    }

    public Ingredient compile() {
        if (value.isEmpty()) return Ingredient.EMPTY;
        if (!matchComponents) return value.compile();
        var predicate = DataComponentPredicate.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, Platform.getFrozenRegistry()),
                componentPredicate).getOrThrow();
        var items = value.getKind() == IngredientValueKind.TAG
                ? BuiltInRegistries.ITEM.getOrCreateTag(TagKey.create(Registries.ITEM, value.getTag()))
                : HolderSet.direct(value.getItem().getItemHolder());
        return new DataComponentIngredient(items, predicate, strict).toVanilla();
    }

    /** 使用代表物品堆，让标签匹配条件也能通过物品的数据组件编辑器修改。 */
    public ItemStack componentPreview() {
        var stack = value.toStack().copyWithCount(1);
        if (stack.isEmpty()) stack = new ItemStack(Items.STONE);
        var predicate = DataComponentPredicate.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, Platform.getFrozenRegistry()),
                componentPredicate).getOrThrow();
        stack.applyComponents(predicate.asPatch());
        return stack;
    }

    public void setPredicate(DataComponentPredicate predicate) {
        componentPredicate = (CompoundTag) DataComponentPredicate.CODEC.encodeStart(
                RegistryOps.create(NbtOps.INSTANCE, Platform.getFrozenRegistry()), predicate).getOrThrow();
    }

    public void updatePredicate(ItemStack stack) {
        var components = DataComponentMap.builder();
        var original = DataComponentPredicate.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, Platform.getFrozenRegistry()),
                componentPredicate).getOrThrow();
        for (var entry : original.asPatch().entrySet()) {
            var value = stack.get(entry.getKey());
            if (value != null) setComponent(components, entry.getKey(), value);
        }
        // 只有显式组件才是匹配条件；导入内容未经修改时，不能把物品默认组件加入条件。
        for (var entry : stack.getComponentsPatch().entrySet()) entry.getValue().ifPresent(component ->
                setComponent(components, entry.getKey(), component));
        setPredicate(DataComponentPredicate.allOf(components.build()));
    }

    @SuppressWarnings("unchecked")
    private static <T> void setComponent(DataComponentMap.Builder builder, DataComponentType<T> type, Object value) {
        builder.set(type, (T) value);
    }
}
