package com.viscript_recipe.compat.kaleidoscope_cookery.canvas;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;

public final class KaleidoscopeSoupBaseUiSupport {
    public static final ResourceLocation DEFAULT_SOUP_BASE = ResourceLocation.withDefaultNamespace("water");

    private KaleidoscopeSoupBaseUiSupport() {
    }

    public static List<ResourceLocation> ids() {
        return SoupBaseManager.getAllSoupBases().keySet().stream()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .toList();
    }

    public static Component displayName(ResourceLocation id) {
        var stack = displayStack(id);
        return stack.isEmpty()
                ? Component.literal(String.valueOf(id))
                : stack.getHoverName().copy().append(Component.literal(" (" + id + ")"));
    }

    public static ItemStack displayStack(ResourceLocation id) {
        var soupBase = SoupBaseManager.getSoupBase(id == null ? DEFAULT_SOUP_BASE : id);
        if (soupBase == null || soupBase.getDisplayStack() == null) {
            return ItemStack.EMPTY;
        }
        return soupBase.getDisplayStack().copyWithCount(1);
    }

    public static ResourceLocation idForStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        for (var entry : SoupBaseManager.getAllSoupBases().entrySet()) {
            if (entry.getValue() != null && entry.getValue().isSoupBase(stack)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
