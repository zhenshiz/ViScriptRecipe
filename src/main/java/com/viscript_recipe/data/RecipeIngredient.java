package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.farmersdelight.FarmersDelightRecipeFactory;
import com.viscript_recipe.recipe.ComponentStackIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

import static com.viscript_recipe.recipe.RecipeHelper.itemFromAbility;
import static com.viscript_recipe.recipe.RecipeHelper.itemsFromTag;

@Getter
@Setter
@Accessors(chain = true)
public class RecipeIngredient implements ISkipDefaultedSerialize, IConfigurable {
    @Persisted
    private IngredientValueKind kind = IngredientValueKind.ITEM;
    @Persisted
    private ItemStack item = new ItemStack(Items.STONE);
    @Persisted
    private ResourceLocation tag = ResourceLocation.withDefaultNamespace("planks");
    @Persisted
    private String itemAbility = "knife_dig";
    @Persisted
    private int count = 1;

    public int getCount() {return kind == IngredientValueKind.ITEM && item.isEmpty() ? 0 : count;}

    public RecipeIngredient setCount(int count) {
        count = Math.max(1, count);
        this.count = count;
        return this;
    }

    public RecipeIngredient setItem(ItemStack stack) {
        // 原料数量由 count 保存，物品模板只保留一件，避免触发 ItemStack Codec 的数量限制。
        item = stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        return this;
    }

    /**请使用工厂方法*/
    @Deprecated
    public RecipeIngredient() {}

    public static RecipeIngredient of() {return new RecipeIngredient();}

    public static RecipeIngredient empty() {return item(ItemStack.EMPTY);}

    public static RecipeIngredient item(Item item) {return item(new ItemStack(item));}

    public static RecipeIngredient item(ItemStack stack) {
        return of().setItem(stack).setCount(stack == null ? 1 : stack.getCount());
    }

    public static RecipeIngredient tag(ResourceLocation tagId) {
        return of().setKind(IngredientValueKind.TAG).setTag(tagId);
    }

    public static RecipeIngredient tag(String tagId) {
        var location = ResourceLocation.tryParse(tagId);
        return RecipeIngredient.tag(location == null ? ResourceLocation.withDefaultNamespace("planks") : location);
    }

    public static RecipeIngredient itemAbility(String itemAbility) {
        return of().setKind(IngredientValueKind.ITEM_ABILITY)
                .setItemAbility(itemAbility == null || itemAbility.isBlank() ? "knife_dig" : itemAbility);
    }

    public Ingredient compile() {
        return switch (kind) {
            case ITEM -> {
                var stack = item.copyWithCount(1);
                if (stack.isEmpty()) yield Ingredient.EMPTY;
                // PotionItem 的默认实例是水瓶，而普通物品原料接受所有药水，不能用默认实例限定匹配。
                if (ItemStack.isSameItemSameComponents(stack, new ItemStack(stack.getItem()))) {
                    yield Ingredient.of(stack.getItem());
                }
                // DataComponentIngredient 无法编码组件删除，会恢复物品默认组件，因此删除语义需要专用原料。
                if (stack.getComponentsPatch().entrySet().stream().anyMatch(component -> component.getValue().isEmpty())) {
                    yield new ComponentStackIngredient(stack).toVanilla();
                }
                yield DataComponentIngredient.of(true, stack);
            }
            case TAG -> {
                if (tag == null) {
                    throw new IllegalArgumentException("Ingredient tag cannot be empty");
                }
                var tagKey = TagKey.create(Registries.ITEM, tag);
                yield Ingredient.of(tagKey);
            }
            case ITEM_ABILITY -> FarmersDelightRecipeFactory.compileItemAbilityIngredient(itemAbility);
        };
    }

    public boolean isEmpty() {
        return switch (kind) {
            case ITEM -> item.isEmpty() || count <= 0;
            case TAG -> tag == null;
            case ITEM_ABILITY -> itemAbility.isBlank();
        };
    }

    public ItemStack toStack() {
        return switch (kind) {
            case ITEM -> item.copyWithCount(count);
            case TAG -> {
                var tagItems = itemsFromTag(tag);
                if (tagItems.length > 0) yield tagItems[0].copyWithCount(count);
                yield ItemStack.EMPTY;
            }
            case ITEM_ABILITY -> itemFromAbility(itemAbility).copyWithCount(count);
        };
    }

    public ItemStack[] getDisplayStacks() {
        var stacks = new ArrayList<ItemStack>();
        switch (kind) {
            case ITEM -> {
                if (!item.isEmpty()) stacks.add(item.copyWithCount(count));
            }
            case TAG -> stacks.addAll(Arrays.stream(itemsFromTag(tag)).map(stack -> stack.copyWithCount(count)).toList());
            case ITEM_ABILITY -> {
                if (!itemAbility.isBlank()) stacks.add(itemFromAbility(itemAbility).copyWithCount(count));
            }
        }
        return stacks.toArray(ItemStack[]::new);
    }

    public RecipeIngredient copy() {
        return of().setKind(kind).setItem(item.copy()).setCount(count).setTag(tag).setItemAbility(itemAbility);
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        if (tag.contains("values") && !tag.getList("values", 10).isEmpty()) {
            tag = tag.getList("values", 10).getCompound(0);
        }
        ISkipDefaultedSerialize.super.deserializeNBT(provider, tag);
    }
}
