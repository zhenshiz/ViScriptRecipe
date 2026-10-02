package com.viscript_recipe.gui.editor;

import com.viscript_recipe.network.RecipeRegistrySnapshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

/** 动态注册表候选项来自当前服务端连接，缓存内容不可变。 */
public final class RecipeRegistryClientData {
    private static volatile Map<ResourceLocation, List<ResourceLocation>> biomeTags = Map.of();
    private static volatile List<ResourceLocation> dimensionTypes = List.of();

    private RecipeRegistryClientData() {
    }

    public static void updateFromServer(CompoundTag snapshot) {
        biomeTags = RecipeRegistrySnapshot.readBiomeTags(snapshot);
        dimensionTypes = RecipeRegistrySnapshot.readDimensionTypes(snapshot);
    }

    public static Map<ResourceLocation, List<ResourceLocation>> biomeTags() {
        return biomeTags;
    }

    public static List<ResourceLocation> dimensionTypes() {
        return dimensionTypes;
    }
}
