package com.viscript_recipe.compat.forbidden_arcanus;

import com.mojang.serialization.MapCodec;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.Ritual;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.CreateItemResult;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.TransmuteInputResult;
import com.stal111.forbidden_arcanus.common.block.entity.forge.ritual.result.UpgradeTierResult;
import com.stal111.forbidden_arcanus.core.registry.FARegistries;
import com.viscript_recipe.ViScriptRecipe;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeEditorTypes.HEPHAESTUS_SMITHING;
import static com.viscript_recipe.compat.forbidden_arcanus.ForbiddenArcanusRecipeEditorTypes.HEPHAESTUS_UPGRADING;

public final class ForgeRitualRecipes {
    private static final String ID_PREFIX = "forbidden_arcanus/ritual/";
    private static final String NAME = "forbidden_arcanus_ritual";
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ViScriptRecipe.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ViScriptRecipe.MOD_ID);
    static final DeferredHolder<RecipeType<?>, RecipeType<ForgeRitualRecipe>> TYPE =
            TYPES.register(NAME, () -> RecipeType.simple(ViScriptRecipe.id(NAME)));
    static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ForgeRitualRecipe>> SERIALIZER =
            SERIALIZERS.register(NAME, Serializer::new);

    private ForgeRitualRecipes() {}

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        SERIALIZERS.register(bus);
    }

    public static boolean isEditorType(ResourceLocation type) {
        return HEPHAESTUS_SMITHING.equals(type) || HEPHAESTUS_UPGRADING.equals(type);
    }

    public static boolean isRitual(RecipeHolder<?> holder) {return holder.value() instanceof ForgeRitualRecipe;}

    static boolean isEditable(Ritual ritual) {
        return ritual.result() instanceof CreateItemResult || ritual.result() instanceof TransmuteInputResult
                || ritual.result() instanceof UpgradeTierResult;
    }

    public static ResourceLocation managerId(ResourceLocation ritualId) {
        return ViScriptRecipe.id(ID_PREFIX + ritualId.getNamespace() + "/" + ritualId.getPath());
    }

    public static ResourceLocation ritualId(ResourceLocation managerId) {
        var path = managerId.getPath();
        if (!managerId.getNamespace().equals(ViScriptRecipe.MOD_ID) || !path.startsWith(ID_PREFIX)) return managerId;
        int slash = path.indexOf('/', ID_PREFIX.length());
        if (slash < 0) return managerId;
        return ResourceLocation.fromNamespaceAndPath(path.substring(ID_PREFIX.length(), slash), path.substring(slash + 1));
    }

    /**
     * 将数据包中的可编辑仪式写入 VSR 基线，保留独立 ID 空间以允许同名的普通配方。
     *
     * @param provider 包含原生仪式的注册表查询上下文
     * @param recipes 接收仪式配方的基线映射
     */
    public static void addBaseline(HolderLookup.Provider provider, Map<ResourceLocation, RecipeHolder<?>> recipes) {
        provider.lookup(FARegistries.RITUAL).ifPresent(registry -> registry.listElements().forEach(holder -> {
            if (!isEditable(holder.value())) return;
            var id = managerId(holder.key().location());
            recipes.put(id, new RecipeHolder<>(id, new ForgeRitualRecipe(holder.value())));
        }));
    }

    public static List<Holder<Ritual>> activeRituals(RecipeManager manager, Stream<Holder.Reference<Ritual>> originals) {
        var remaining = new LinkedHashMap<ResourceLocation, Ritual>();
        for (var holder : manager.getAllRecipesFor(TYPE.get())) {
            remaining.put(ritualId(holder.id()), holder.value().ritual());
        }
        var result = new ArrayList<Holder<Ritual>>();
        originals.forEach(holder -> {
            if (!isEditable(holder.value())) {
                result.add(holder);
                return;
            }
            var ritual = remaining.remove(holder.key().location());
            if (ritual != null) result.add(ritual == holder.value() ? holder : Holder.direct(ritual));
        });
        remaining.values().forEach(ritual -> result.add(Holder.direct(ritual)));
        return result;
    }

    public static Stream<Ritual> visibleRituals(RecipeManager manager) {
        return manager.getAllRecipesFor(TYPE.get()).stream().map(holder -> holder.value().ritual());
    }

    public static @Nullable ResourceLocation activeId(RecipeManager manager, Holder<Ritual> ritual) {
        return manager.getAllRecipesFor(TYPE.get()).stream()
                .filter(holder -> holder.value().ritual() == ritual.value())
                .findFirst().map(holder -> ritualId(holder.id())).orElse(null);
    }

    public interface ActiveId {
        @Nullable ResourceLocation viscriptRecipe$getRitualId();
        void viscriptRecipe$setRitualId(@Nullable ResourceLocation id);
    }

    private static final class Serializer implements RecipeSerializer<ForgeRitualRecipe> {
        private static final MapCodec<ForgeRitualRecipe> CODEC =
                Ritual.DIRECT_CODEC.fieldOf("ritual").xmap(ForgeRitualRecipe::new, ForgeRitualRecipe::ritual);
        private static final StreamCodec<RegistryFriendlyByteBuf, ForgeRitualRecipe> STREAM_CODEC =
                ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

        @Override public MapCodec<ForgeRitualRecipe> codec() {return CODEC;}
        @Override public StreamCodec<RegistryFriendlyByteBuf, ForgeRitualRecipe> streamCodec() {return STREAM_CODEC;}
    }
}
