package com.viscript_recipe.recipe;

import com.mojang.serialization.MapCodec;
import com.viscript_recipe.ViScriptRecipe;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.stream.Stream;

public final class ComponentStackIngredient implements ICustomIngredient {
    private static final MapCodec<ComponentStackIngredient> CODEC = ItemStack.SINGLE_ITEM_CODEC
            .fieldOf("stack").xmap(ComponentStackIngredient::new, ingredient -> ingredient.stack);
    private static final DeferredRegister<IngredientType<?>> TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, ViScriptRecipe.MOD_ID);
    private static final DeferredHolder<IngredientType<?>, IngredientType<ComponentStackIngredient>> TYPE =
            TYPES.register("component_stack", () -> new IngredientType<>(CODEC));

    private final ItemStack stack;

    public ComponentStackIngredient(ItemStack stack) {
        this.stack = stack.copyWithCount(1);
    }

    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
    }

    @Override
    public boolean test(ItemStack candidate) {
        return ItemStack.isSameItemSameComponents(stack, candidate);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Stream.of(stack.copy());
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
                && ItemStack.isSameItemSameComponents(stack, ingredient.stack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(stack);
    }
}
