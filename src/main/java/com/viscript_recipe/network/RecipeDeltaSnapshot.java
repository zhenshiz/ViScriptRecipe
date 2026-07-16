package com.viscript_recipe.network;

import com.mojang.serialization.Codec;
import com.viscript_recipe.compat.irons_spellbooks.IronArcaneAnvilOverrideManager;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Compact recipe state transfer used by the fast reload path.
 *
 * <p>The RPC layer transports this object as a {@link CompoundTag}; recipes still use their
 * registered vanilla codecs, so optional recipe serializers keep their normal network contract.
 */
public record RecipeDeltaSnapshot(
        long baseRevision,
        long revision,
        boolean baseline,
        boolean showcaseOnly,
        boolean arcaneAnvilChanged,
        List<ResourceLocation> removedRecipeIds,
        List<RecipeHolder<?>> upsertedRecipes,
        Map<ResourceLocation, ResourceLocation> managedEditorTypes,
        Map<ResourceLocation, ResourceLocation> recipeTypeHints,
        List<IronArcaneAnvilOverrideManager.CompiledRecipe> arcaneAnvilRecipes
) {
    public static final int PROTOCOL_VERSION = 1;

    public RecipeDeltaSnapshot {
        removedRecipeIds = List.copyOf(removedRecipeIds);
        upsertedRecipes = List.copyOf(upsertedRecipes);
        managedEditorTypes = Map.copyOf(managedEditorTypes);
        recipeTypeHints = Map.copyOf(recipeTypeHints);
        arcaneAnvilRecipes = List.copyOf(arcaneAnvilRecipes);
    }

    public int changedRecipeCount() {
        return removedRecipeIds.size() + upsertedRecipes.size();
    }

    public boolean hasChanges() {
        return changedRecipeCount() > 0 || arcaneAnvilChanged;
    }

    public CompoundTag serialize(HolderLookup.Provider provider) {
        var root = new CompoundTag();
        root.putInt("protocol", PROTOCOL_VERSION);
        root.putLong("base_revision", baseRevision);
        root.putLong("revision", revision);
        root.putBoolean("baseline", baseline);
        root.putBoolean("showcase_only", showcaseOnly);
        root.putBoolean("arcane_anvil_changed", arcaneAnvilChanged);

        var removed = new ListTag();
        removedRecipeIds.forEach(id -> removed.add(StringTag.valueOf(id.toString())));
        root.put("removed", removed);

        var upserted = new ListTag();
        for (var holder : upsertedRecipes) {
            var entry = new CompoundTag();
            entry.putString("id", holder.id().toString());
            entry.put("recipe", encode(provider, Recipe.CODEC, holder.value(), "recipe " + holder.id()));
            upserted.add(entry);
        }
        root.put("upserted", upserted);
        root.put("managed_editor_types", encodeResourceLocationMap(managedEditorTypes, "editor_type"));
        root.put("recipe_type_hints", encodeResourceLocationMap(recipeTypeHints, "recipe_type"));

        var arcaneAnvil = new ListTag();
        for (var recipe : arcaneAnvilRecipes) {
            var entry = new CompoundTag();
            entry.putString("id", recipe.id().toString());
            entry.put("input", encode(provider, Ingredient.CODEC, recipe.input(), "arcane anvil input " + recipe.id()));
            entry.put("material", encode(provider, Ingredient.CODEC, recipe.material(), "arcane anvil material " + recipe.id()));
            entry.put("result", encode(provider, ItemStack.CODEC, recipe.result(), "arcane anvil result " + recipe.id()));
            arcaneAnvil.add(entry);
        }
        root.put("arcane_anvil", arcaneAnvil);
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
            var recipe = decode(provider, Recipe.CODEC, entry.get("recipe"), "recipe " + id);
            upserted.add(new RecipeHolder<>(id, recipe));
        }

        var arcaneAnvil = new ArrayList<IronArcaneAnvilOverrideManager.CompiledRecipe>();
        for (var tag : root.getList("arcane_anvil", Tag.TAG_COMPOUND)) {
            var entry = (CompoundTag) tag;
            var id = ResourceLocation.parse(entry.getString("id"));
            var input = decode(provider, Ingredient.CODEC, entry.get("input"), "arcane anvil input " + id);
            var material = decode(provider, Ingredient.CODEC, entry.get("material"), "arcane anvil material " + id);
            var result = decode(provider, ItemStack.CODEC, entry.get("result"), "arcane anvil result " + id);
            arcaneAnvil.add(new IronArcaneAnvilOverrideManager.CompiledRecipe(id, input, material, result));
        }

        return new RecipeDeltaSnapshot(
                root.getLong("base_revision"),
                root.getLong("revision"),
                root.getBoolean("baseline"),
                root.getBoolean("showcase_only"),
                root.getBoolean("arcane_anvil_changed"),
                removed,
                upserted,
                decodeResourceLocationMap(root.getList("managed_editor_types", Tag.TAG_COMPOUND), "editor_type"),
                decodeResourceLocationMap(root.getList("recipe_type_hints", Tag.TAG_COMPOUND), "recipe_type"),
                arcaneAnvil
        );
    }

    public static Tag encodeRecipe(HolderLookup.Provider provider, RecipeHolder<?> holder) {
        return encode(provider, Recipe.CODEC, holder.value(), "recipe " + holder.id());
    }

    public static Tag encodeArcaneAnvilRecipes(
            HolderLookup.Provider provider,
            List<IronArcaneAnvilOverrideManager.CompiledRecipe> recipes
    ) {
        var encoded = new ListTag();
        for (var recipe : recipes) {
            var entry = new CompoundTag();
            entry.putString("id", recipe.id().toString());
            entry.put("input", encode(provider, Ingredient.CODEC, recipe.input(), "arcane anvil input " + recipe.id()));
            entry.put("material", encode(provider, Ingredient.CODEC, recipe.material(), "arcane anvil material " + recipe.id()));
            entry.put("result", encode(provider, ItemStack.CODEC, recipe.result(), "arcane anvil result " + recipe.id()));
            encoded.add(entry);
        }
        return encoded;
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
