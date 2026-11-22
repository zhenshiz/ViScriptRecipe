package com.viscript_recipe.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.viscript_recipe.ViScriptRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.stream.Stream;

public final class ComponentStackIngredient implements ICustomIngredient {
    private static final MapCodec<ComponentStackIngredient> CODEC = RecordCodecBuilder.<ComponentStackIngredient>create(instance -> instance.group(
            ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ingredient -> ingredient.item),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY)
                    .forGetter(ingredient -> ingredient.components)
    ).apply(instance, ComponentStackIngredient::new)).fieldOf("stack");
    private static final DeferredRegister<IngredientType<?>> TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, ViScriptRecipe.MOD_ID);
    private static final DeferredHolder<IngredientType<?>, IngredientType<ComponentStackIngredient>> TYPE =
            TYPES.register("component_stack", () -> new IngredientType<>(CODEC));

    private final Holder<Item> item;
    private final DataComponentPatch components;
    private final boolean removesComponents;

    public ComponentStackIngredient(ItemStack stack) {
        this(stack.getItemHolder(), stack.getComponentsPatch());
    }

    private ComponentStackIngredient(Holder<Item> item, DataComponentPatch components) {
        this.item = item;
        this.components = components;
        this.removesComponents = components.entrySet().stream().anyMatch(component -> component.getValue().isEmpty());
    }

    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
    }

    @Override
    public boolean test(ItemStack candidate) {
        if (!candidate.is(item.value())) return false;
        var template = new ItemStack(item, 1, components);
        // 群峦等模组会在配方编译后补齐默认组件；此时普通模板仍应按物品匹配，显式删除则保持严格匹配。
        if (!removesComponents && ItemStack.isSameItemSameComponents(template, new ItemStack(item))) return true;
        return ItemStack.isSameItemSameComponents(template, candidate);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Stream.of(new ItemStack(item, 1, components));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TYPE.get();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ComponentStackIngredient ingredient
                && item.equals(ingredient.item) && components.equals(ingredient.components);
    }

    @Override
    public int hashCode() {
        return 31 * item.hashCode() + components.hashCode();
    }
}
