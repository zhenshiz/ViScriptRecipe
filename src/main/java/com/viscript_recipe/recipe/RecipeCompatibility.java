package com.viscript_recipe.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/** 保存已加载模块针对原生配方声明的运行期处理。 */
public final class RecipeCompatibility {
    private static final List<BiConsumer<RecipeManager, ResourceManager>> AFTER_SERVER_APPLY = new ArrayList<>();
    private static final List<Consumer<RecipeManager>> AFTER_SERVER_RELOAD = new ArrayList<>();
    private static final List<Consumer<RecipeManager>> AFTER_CLIENT_SYNC = new ArrayList<>();
    private static final Map<ResourceLocation, Function<ResourceLocation, List<ResourceLocation>>> REMOVAL_IDS = new HashMap<>();
    private static final Map<ResourceLocation, SnapshotEncoder> SNAPSHOT_ENCODERS = new HashMap<>();
    private static final List<Predicate<ResourceLocation>> STREAM_SERIALIZERS = new ArrayList<>();

    private RecipeCompatibility() {}

    public static void afterServerApply(RecipeManager manager, @Nullable ResourceManager resources) {
        AFTER_SERVER_APPLY.forEach(listener -> listener.accept(manager, resources));
    }

    public static void afterServerReload(RecipeManager manager) {
        AFTER_SERVER_RELOAD.forEach(listener -> listener.accept(manager));
    }

    public static void afterClientSync(RecipeManager manager) {
        AFTER_CLIENT_SYNC.forEach(listener -> listener.accept(manager));
    }

    public static List<ResourceLocation> removalIds(ResourceLocation editorType, ResourceLocation id) {
        var ids = REMOVAL_IDS.get(editorType);
        return ids == null ? List.of(id) : ids.apply(id);
    }

    public static @Nullable SnapshotEncoder snapshotEncoder(RecipeHolder<?> holder) {
        return SNAPSHOT_ENCODERS.get(serializerId(holder));
    }

    public static boolean usesNativeStream(RecipeHolder<?> holder) {
        var id = serializerId(holder);
        return id != null && STREAM_SERIALIZERS.stream().anyMatch(filter -> filter.test(id));
    }

    private static ResourceLocation serializerId(RecipeHolder<?> holder) {
        return BuiltInRegistries.RECIPE_SERIALIZER.getKey(holder.value().getSerializer());
    }

    @FunctionalInterface
    public interface SnapshotEncoder {
        Tag encode(HolderLookup.Provider provider, RecipeHolder<?> holder);
    }

    public static final class Registration {
        /**
         * 注册每次服务端应用 VSR 配方后调用的处理。
         *
         * @param listener 接收更新后的配方管理器和可为空的当前资源管理器
         */
        public void afterServerApply(BiConsumer<RecipeManager, @Nullable ResourceManager> listener) {
            AFTER_SERVER_APPLY.add(listener);
        }

        /**
         * 注册执行 VSR 专用重载命令后调用的处理。
         *
         * @param listener 接收服务端配方管理器的处理
         */
        public void afterServerReload(Consumer<RecipeManager> listener) {
            AFTER_SERVER_RELOAD.add(listener);
        }

        /**
         * 注册客户端应用服务端配方同步后调用的处理。
         *
         * @param listener 接收客户端配方管理器的处理
         */
        public void afterClientSync(Consumer<RecipeManager> listener) {
            AFTER_CLIENT_SYNC.add(listener);
        }

        /**
         * 指定删除一个编辑器条目时应删除的全部原生配方 ID。
         *
         * @param editorType 适用的编辑器类型 ID
         * @param ids 从条目 ID 计算待删除配方 ID 的函数
         */
        public void removalIds(ResourceLocation editorType, Function<ResourceLocation, List<ResourceLocation>> ids) {
            REMOVAL_IDS.put(editorType, ids);
        }

        /**
         * 指定增量比较和同步所用的配方编码器。产物应能由原生配方 Codec 读取。
         *
         * @param serializer 适用的原生配方序列化器 ID
         * @param encoder 编码配方快照的函数
         */
        public void snapshotEncoder(ResourceLocation serializer, SnapshotEncoder encoder) {
            SNAPSHOT_ENCODERS.put(serializer, encoder);
        }

        /**
         * 指定增量同步时直接使用原生网络 Codec 的序列化器。
         *
         * @param filter 匹配序列化器 ID 的条件
         */
        public void useNativeStream(Predicate<ResourceLocation> filter) {
            STREAM_SERIALIZERS.add(filter);
        }
    }
}
