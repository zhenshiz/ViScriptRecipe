package com.viscript_recipe.recipe.vanilla;

import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib2.utils.LDLibExtraCodecs;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.vanilla.VanillaEventRecipeData;
import com.viscript_recipe.data.vanilla.VanillaEventRecipeEditorTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.nikdo53.neobackports.io.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.stream.Stream;

public final class VanillaEventRecipeRuntime {
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, ViScriptRecipe.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, ViScriptRecipe.MOD_ID);
    public static final RegistryObject<RecipeType<VanillaEventRecipe>> ANVIL_TYPE = TYPES.register("anvil", () -> type("anvil"));
    public static final RegistryObject<RecipeType<VanillaEventRecipe>> BREWING_TYPE = TYPES.register("brewing", () -> type("brewing"));
    public static final RegistryObject<RecipeSerializer<VanillaEventRecipe>> ANVIL_SERIALIZER =
            SERIALIZERS.register("anvil", () -> new Serializer(VanillaEventRecipeEditorTypes.ANVIL));
    public static final RegistryObject<RecipeSerializer<VanillaEventRecipe>> BREWING_SERIALIZER =
            SERIALIZERS.register("brewing", () -> new Serializer(VanillaEventRecipeEditorTypes.BREWING));

    private VanillaEventRecipeRuntime() {}

    public static void register() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        TYPES.register(modEventBus);
        SERIALIZERS.register(modEventBus);
        modEventBus.addListener(VanillaEventRecipeRuntime::registerBrewing);
        MinecraftForge.EVENT_BUS.addListener(VanillaEventRecipeRuntime::updateAnvil);
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
            if (name.isBlank()) result.resetHoverName();
            else if (!name.equals(event.getLeft().getHoverName().getString())) result.setHoverName(Component.literal(name));
        }
        event.setOutput(result);
        event.setCost(recipe.levelCost());
        event.setMaterialCost(recipe.materialCost());
    }

    private static void registerBrewing(FMLCommonSetupEvent event) {
        // 注册一次动态适配器，每次匹配读取当前配方；重载、同步和退出世界无需另外维护规则缓存。
        event.enqueueWork(() -> {
            BrewingRecipeRegistry.addRecipe(new IBrewingRecipe() {
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
                .sorted(Comparator.comparing(holder -> holder.getId().toString()));
    }

    private static final class ClientRecipes {
        private static RecipeManager manager() {
            var level = net.minecraft.client.Minecraft.getInstance().level;
            return level == null ? null : level.getRecipeManager();
        }
    }

    private static final class Serializer implements RecipeSerializer<VanillaEventRecipe> {
        private final Codec<VanillaEventRecipe> codec;
        private final StreamCodec<VanillaEventRecipe> streamCodec;

        private Serializer(ResourceLocation type) {
            codec = PersistedParser.createCodec(VanillaEventRecipeData::new)
                    .xmap(data -> new VanillaEventRecipe(type, data), VanillaEventRecipe::data);
            streamCodec = PersistedParser.createStreamCodec(VanillaEventRecipeData::new).map(data -> new VanillaEventRecipe(type, data), VanillaEventRecipe::data);
        }

        @Override
        public VanillaEventRecipe fromJson(ResourceLocation id, JsonObject jsonObject) {
            var parsed = codec.parse(new Dynamic<>(JsonOps.INSTANCE, jsonObject));
            return LDLibExtraCodecs.getOrThrow(parsed).setId(id);
        }

        @Override
        public @Nullable VanillaEventRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            return streamCodec.decode(buf).setId(id);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, VanillaEventRecipe recipe) {
            streamCodec.encode(buf, recipe);
        }
    }
}
