package com.viscript_recipe.data;

import com.lowdragmc.lowdraglib2.configurator.IConfigurable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.viscript_lib.util.ISkipDefaultedSerialize;
import com.viscript_recipe.compat.farmersdelight.FarmersDelightRecipeFactory;
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
import java.util.List;

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
    private ResourceLocation tag = ResourceLocation.fromNamespaceAndPath("minecraft", "planks");
    @Persisted
    private String itemAbility = "knife_dig";

    /**请使用工厂方法*/
    private RecipeIngredient() {}

    public static RecipeIngredient of() {return new RecipeIngredient();}

    public static RecipeIngredient empty() {return item(ItemStack.EMPTY);}

    public static RecipeIngredient item(Item item) {return item(new ItemStack(item));}

    public static RecipeIngredient item(ItemStack stack) {
        return of().setItem(stack == null ? ItemStack.EMPTY : stack.copyWithCount(1));
    }

    public static RecipeIngredient itemWithCount(ItemStack stack) {
        return of().setItem(stack == null ? ItemStack.EMPTY : stack.copy());
    }

    public static RecipeIngredient tag(ResourceLocation tagId) {
        return of().setKind(IngredientValueKind.TAG).setTag(tagId);
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
                if (ItemStack.isSameItemSameComponents(stack, stack.getItem().getDefaultInstance())) {
                    yield Ingredient.of(stack.getItem());
                }
                yield DataComponentIngredient.of(true, stack);
            }
            case TAG -> {
                if (tag == null) {
                    throw new IllegalArgumentException("Ingredient tag cannot be empty");
                }
                var tagKey = TagKey.create(Registries.ITEM, tag);
                if (BuiltInRegistries.ITEM.getTag(tagKey).isEmpty()) {
                    throw new IllegalArgumentException("Unknown item tag: " + tag);
                }
                yield Ingredient.of(tagKey);
            }
            case ITEM_ABILITY -> FarmersDelightRecipeFactory.compileItemAbilityIngredient(itemAbility);
        };
    }

    public boolean isEmpty() {
        switch (kind) {
            case ITEM ->         { if (!item.isEmpty()) return false; }
            case TAG ->          { if (tag != null) return false; }
            case ITEM_ABILITY -> { if (!itemAbility.isBlank()) return false; }
        }
        return true;
    }

    public ItemStack toStack() {
        return switch (kind) {
            case ITEM -> item.copy();
            case TAG -> {
                var tagItems = itemsFromTag(tag);
                if (tagItems.length > 0) yield tagItems[0].copy();
                yield ItemStack.EMPTY;
            }
            case ITEM_ABILITY -> itemFromAbility(itemAbility);
        };
    }

    public ItemStack[] getDisplayStacks() {
        var stacks = new ArrayList<ItemStack>();
        switch (kind) {
            case ITEM -> {
                if (!item.isEmpty()) stacks.add(item.copyWithCount(1));
            }
            case TAG -> stacks.addAll(List.of(itemsFromTag(tag)));
            case ITEM_ABILITY -> {
                if (!itemAbility.isBlank()) stacks.add(itemFromAbility(itemAbility));
            }
        }
        return stacks.toArray(ItemStack[]::new);
    }

    public RecipeIngredient copy() {
        return of().setKind(kind).setItem(item.copy()).setTag(tag).setItemAbility(itemAbility);
    }

    public RecipeIngredient copyWithCount(int count) {
        return of().setKind(kind).setItem(item.copyWithCount(count)).setTag(tag).setItemAbility(itemAbility);
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        if (tag.contains("values") && !tag.getList("values", 10).isEmpty()) {
            tag = tag.getList("values", 10).getCompound(0);
        }
        ISkipDefaultedSerialize.super.deserializeNBT(provider, tag);
    }
}
