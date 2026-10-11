package com.viscript_recipe.recipe.vanilla;

import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.MapCodec;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.vanilla.VanillaEventRecipeData;
import com.viscript_recipe.data.vanilla.VanillaEventRecipeEditorTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Comparator;
import java.util.stream.Stream;

public final class VanillaEventRecipeRuntime {
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ViScriptRecipe.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ViScriptRecipe.MOD_ID);
    public static final DeferredHolder<RecipeType<?>, RecipeType<VanillaEventRecipe>> ANVIL_TYPE = TYPES.register("anvil", () -> type("anvil"));
    public static final DeferredHolder<RecipeType<?>, RecipeType<VanillaEventRecipe>> BREWING_TYPE = TYPES.register("brewing", () -> type("brewing"));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<VanillaEventRecipe>> ANVIL_SERIALIZER =
            SERIALIZERS.register("anvil", () -> new Serializer(VanillaEventRecipeEditorTypes.ANVIL));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<VanillaEventRecipe>> BREWING_SERIALIZER =
            SERIALIZERS.register("brewing", () -> new Serializer(VanillaEventRecipeEditorTypes.BREWING));

    private VanillaEventRecipeRuntime() {}

    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
        SERIALIZERS.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(VanillaEventRecipeRuntime::updateAnvil);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, VanillaEventRecipeRuntime::registerBrewing);
    }

    private static RecipeType<VanillaEventRecipe> type(String name) {
        return new RecipeType<>() {
            @Override
            public String toString() {return ViScriptRecipe.id(name).toString();}
        };
    }

    private static void updateAnvil(AnvilUpdateEvent event) {
        var manager = event.getPlayer().level().getRecipeManager();
        var match = recipes(manager, ANVIL_TYPE.get())
                .filter(recipe -> recipe.matchesItems(event.getLeft(), event.getRight())).findFirst();
        if (match.isEmpty()) return;
        var recipe = match.get();
        // 事件不会检查材料数量，必须先拦截不足的组合，避免原版吞掉整个不足量的材料栈。
        if (event.getRight().getCount() < recipe.materialCost()) {
            event.setCanceled(true);
            return;
        }
        var result = recipe.result();
        var name = event.getName();
        if (name != null) {
            if (name.isBlank()) result.remove(DataComponents.CUSTOM_NAME);
            else if (!name.equals(event.getLeft().getHoverName().getString())) result.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        }
        event.setOutput(result);
        event.setCost(recipe.levelCost());
        event.setMaterialCost(recipe.materialCost());
    }

    private static void registerBrewing(RegisterBrewingRecipesEvent event) {
        // 注册一次动态适配器，每次匹配读取当前配方；重载、同步和退出世界无需另外维护规则缓存。
        event.getBuilder().addRecipe(new IBrewingRecipe() {
            @Override
            public boolean isInput(ItemStack input) {return brewingRecipes().anyMatch(recipe -> recipe.acceptsInput(input));}
            @Override
            public boolean isIngredient(ItemStack ingredient) {return brewingRecipes().anyMatch(recipe -> recipe.acceptsMaterial(ingredient));}
            @Override
            public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
                return brewingRecipes().filter(recipe -> recipe.matchesItems(input, ingredient))
                        .findFirst().map(VanillaEventRecipe::result).orElse(ItemStack.EMPTY);
            }
        });
    }

    private static Stream<VanillaEventRecipe> brewingRecipes() {
        RecipeManager manager = null;
        if (EffectiveSide.get().isServer()) {
            var server = ServerLifecycleHooks.getCurrentServer();
            if (server != null) manager = server.getRecipeManager();
        } else if (FMLEnvironment.dist.isClient()) manager = ClientRecipes.manager();
        return manager == null ? Stream.empty() : recipes(manager, BREWING_TYPE.get());
    }

    private static Stream<VanillaEventRecipe> recipes(RecipeManager manager, RecipeType<VanillaEventRecipe> type) {
        return manager.getAllRecipesFor(type).stream()
                .sorted(Comparator.comparing(holder -> holder.id().toString())).map(RecipeHolder::value);
    }

    private static final class ClientRecipes {
        private static RecipeManager manager() {
            var level = net.minecraft.client.Minecraft.getInstance().level;
            return level == null ? null : level.getRecipeManager();
        }
    }

    private static final class Serializer implements RecipeSerializer<VanillaEventRecipe> {
        private final MapCodec<VanillaEventRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, VanillaEventRecipe> streamCodec;

        private Serializer(ResourceLocation type) {
            codec = PersistedParser.createMapCodec(VanillaEventRecipeData::new)
                    .xmap(data -> new VanillaEventRecipe(type, data), VanillaEventRecipe::data);
            streamCodec = PersistedParser.createStreamCodec(VanillaEventRecipeData::new)
                    .<RegistryFriendlyByteBuf>cast().map(data -> new VanillaEventRecipe(type, data), VanillaEventRecipe::data);
        }

        @Override
        public MapCodec<VanillaEventRecipe> codec() {return codec;}
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, VanillaEventRecipe> streamCodec() {return streamCodec;}
    }
}
