package com.viscript_recipe.gui.editor;

import com.viscript_recipe.network.StructureTagSnapshot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

/** 结构标签目录来自当前服务端会话，缓存内容不可变。 */
public final class StructureTagClientData {
    private static volatile Map<ResourceLocation, List<ResourceLocation>> tags = Map.of();

    private StructureTagClientData() {
    }

    public static void updateFromServer(CompoundTag snapshot) {
        tags = StructureTagSnapshot.read(snapshot);
    }

    static Map<ResourceLocation, List<ResourceLocation>> tags() {
        return tags;
    }
}
