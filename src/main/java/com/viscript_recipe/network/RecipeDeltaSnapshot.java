package com.viscript_recipe.network;

import com.mojang.serialization.Codec;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.compat.tacz.TaczRecipeFactory;
import com.viscript_recipe.compat.lychee.LycheeRecipeFactory;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 增量重载通过 RPC 传输 {@link CompoundTag}；配方仍使用已注册的原版 Codec，
 * 以保留可选配方序列化器原有的网络协议。
 */
public record RecipeDeltaSnapshot(
        long baseRevision,
        long revision,
        boolean baseline,
        boolean showcaseOnly,
        List<ResourceLocation> removedRecipeIds,
        List<RecipeHolder<?>> upsertedRecipes,
        Map<ResourceLocation, ResourceLocation> managedEditorTypes,
        Map<ResourceLocation, ResourceLocation> recipeTypeHints
) {
    public static final int PROTOCOL_VERSION = 1;

    public RecipeDeltaSnapshot {
        removedRecipeIds = List.copyOf(removedRecipeIds);
        upsertedRecipes = List.copyOf(upsertedRecipes);
        managedEditorTypes = Map.copyOf(managedEditorTypes);
        recipeTypeHints = Map.copyOf(recipeTypeHints);
    }

    public int changedRecipeCount() {
        return removedRecipeIds.size() + upsertedRecipes.size();
    }

    public boolean hasChanges() {
        return changedRecipeCount() > 0;
    }

    public CompoundTag serialize(HolderLookup.Provider provider) {
        var root = new CompoundTag();
        root.putInt("protocol", PROTOCOL_VERSION);
        root.putLong("base_revision", baseRevision);
        root.putLong("revision", revision);
        root.putBoolean("baseline", baseline);
        root.putBoolean("showcase_only", showcaseOnly);

        var removed = new ListTag();
        removedRecipeIds.forEach(id -> removed.add(StringTag.valueOf(id.toString())));
        root.put("removed", removed);

        var upserted = new ListTag();
        for (var holder : upsertedRecipes) {
            var entry = new CompoundTag();
            entry.putString("id", holder.id().toString());
            var serializerId = BuiltInRegistries.RECIPE_SERIALIZER.getKey(holder.value().getSerializer());
            if (ViScriptRecipe.isModLoaded("lychee") && serializerId != null && serializerId.getNamespace().equals("lychee")) {
                // Lychee 的网络 Codec 会过滤 preventSync 动作，不能用持久化 Codec 直接发送。
                entry.putByteArray("lychee_stream", LycheeRecipeFactory.encodeForSync(provider, holder));
            } else entry.put("recipe", encodeRecipe(provider, holder));
            upserted.add(entry);
        }
        root.put("upserted", upserted);
        root.put("managed_editor_types", encodeResourceLocationMap(managedEditorTypes, "editor_type"));
        root.put("recipe_type_hints", encodeResourceLocationMap(recipeTypeHints, "recipe_type"));

        return root;
    }

    public static RecipeDeltaSnapshot deserialize(HolderLookup.Provider provider, CompoundTag root) {
        var protocol = root.getInt("protocol");
        if (protocol != PROTOCOL_VERSION) {
            throw new IllegalArgumentException("Unsupported recipe delta protocol " + protocol);
        }

        var removed = new ArrayList<ResourceLocation>();
        for (var tag : root.getList("removed", Tag.TAG_STRING)) {
            removed.add(ResourceLocation.parse(tag.getAsString()));
        }

        var upserted = new ArrayList<RecipeHolder<?>>();
        for (var tag : root.getList("upserted", Tag.TAG_COMPOUND)) {
            var entry = (CompoundTag) tag;
            var id = ResourceLocation.parse(entry.getString("id"));
            if (ViScriptRecipe.isModLoaded("lychee") && entry.contains("lychee_stream", Tag.TAG_BYTE_ARRAY)) {
                upserted.add(LycheeRecipeFactory.decodeFromSync(provider, entry.getByteArray("lychee_stream")));
                continue;
            }
            var recipe = decode(provider, Recipe.CODEC, entry.get("recipe"), "recipe " + id);
            upserted.add(new RecipeHolder<>(id, recipe));
        }

        return new RecipeDeltaSnapshot(
                root.getLong("base_revision"),
                root.getLong("revision"),
                root.getBoolean("baseline"),
                root.getBoolean("showcase_only"),
                removed,
                upserted,
                decodeResourceLocationMap(root.getList("managed_editor_types", Tag.TAG_COMPOUND), "editor_type"),
                decodeResourceLocationMap(root.getList("recipe_type_hints", Tag.TAG_COMPOUND), "recipe_type")
        );
    }

    public static Tag encodeRecipe(HolderLookup.Provider provider, RecipeHolder<?> holder) {
        // TACZ 的产物 Codec 未实现写入，但原生解码器支持保留完整物品组件的 custom 格式。
        if (ViScriptRecipe.isModLoaded("tacz") && ResourceLocation.parse("tacz:gun_smith_table_crafting")
                .equals(BuiltInRegistries.RECIPE_SERIALIZER.getKey(holder.value().getSerializer()))) {
            return TaczRecipeFactory.encodeSnapshot(holder.value(), provider);
        }
        return encode(provider, Recipe.CODEC, holder.value(), "recipe " + holder.id());
    }

    private static ListTag encodeResourceLocationMap(
            Map<ResourceLocation, ResourceLocation> values,
            String valueKey
    ) {
        var encoded = new ListTag();
        values.forEach((id, value) -> {
            var entry = new CompoundTag();
            entry.putString("id", id.toString());
            entry.putString(valueKey, value.toString());
            encoded.add(entry);
        });
        return encoded;
    }

    private static Map<ResourceLocation, ResourceLocation> decodeResourceLocationMap(
            ListTag encoded,
            String valueKey
    ) {
        var values = new LinkedHashMap<ResourceLocation, ResourceLocation>();
        for (var tag : encoded) {
            var entry = (CompoundTag) tag;
            values.put(
                    ResourceLocation.parse(entry.getString("id")),
                    ResourceLocation.parse(entry.getString(valueKey))
            );
        }
        return values;
    }

    private static <T> Tag encode(
            HolderLookup.Provider provider,
            Codec<T> codec,
            T value,
            String description
    ) {
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        return codec.encodeStart(ops, value)
                .getOrThrow(message -> new IllegalArgumentException("Failed to encode " + description + ": " + message));
    }

    private static <T> T decode(
            HolderLookup.Provider provider,
            Codec<T> codec,
            Tag tag,
            String description
    ) {
        if (tag == null) {
            throw new IllegalArgumentException("Missing encoded " + description);
        }
        var ops = provider.createSerializationContext(NbtOps.INSTANCE);
        return codec.parse(ops, tag)
                .getOrThrow(message -> new IllegalArgumentException("Failed to decode " + description + ": " + message));
    }
}
