package com.viscript_recipe.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record RecipeEditorCategory(
        ResourceLocation id,
        String translationKey,
        String ownerModId,
        ResourceLocation defaultType,
        ResourceLocation workstationItemId
) {
    public RecipeEditorCategory(ResourceLocation id, String translationKey, String ownerModId, ResourceLocation defaultType) {
        this(id, translationKey, ownerModId, defaultType, id);
    }

    public static RecipeEditorCategory of(ResourceLocation id, String translationKey,
                                          String ownerModId, ResourceLocation defaultType) {
        return new RecipeEditorCategory(id, translationKey, ownerModId, defaultType);
    }

    public static RecipeEditorCategory of(ResourceLocation id, String translationKey, String ownerModId,
                                          ResourceLocation defaultType, ResourceLocation workstationItemId) {
        return new RecipeEditorCategory(id, translationKey, ownerModId, defaultType, workstationItemId);
    }

    public Component displayName() {
        return Component.translatable(translationKey);
    }

    public ItemStack workstationStack() {
        if (workstationItemId == null || !BuiltInRegistries.ITEM.containsKey(workstationItemId)) {
            return ItemStack.EMPTY;
        }
        var item = BuiltInRegistries.ITEM.get(workstationItemId);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    public Component ownerName() {
        return Component.literal(ownerModId);
    }
}
