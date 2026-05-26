package com.viscript_recipe.command;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.mojang.brigadier.CommandDispatcher;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.gui.editor.RecipeEditor;
import com.viscript_recipe.recipe.RecipeOverrideManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagNetworkSerialization;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = ViScriptRecipe.MOD_ID)
public final class ViScriptRecipeCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(ViScriptRecipe.MOD_ID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("editor")
                        .executes(context -> {
                            var server = context.getSource().getServer();
                            var player = context.getSource().getPlayer();
                            if (player == null) {
                                return 0;
                            }
                            if (!server.isSingleplayer()) {
                                context.getSource().sendFailure(Component.translatable("commands.viscript_recipe.editor.singleplayer_only"));
                                return 0;
                            }
                            return PlayerUIMenuType.openUI(player, RecipeEditor.WINDOW_ID) ? 1 : 0;
                        }))
                .then(Commands.literal("reload")
                        .executes(context -> reloadRecipes(context.getSource())))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource()))));
    }

    private static int reloadRecipes(CommandSourceStack source) {
        var server = source.getServer();
        var result = RecipeOverrideManager.reload(server.getRecipeManager(), server.registryAccess());
        syncReloadDataToPlayers(server);
        source.sendSuccess(() -> Component.translatable(
                "commands.viscript_recipe.reload.success",
                result.fileCount(),
                result.entryCount(),
                result.enabledEntryCount(),
                result.appliedEntryCount(),
                result.skippedEntryCount(),
                result.failedEntryCount(),
                result.resultRecipeCount()
        ), true);
        return 1;
    }

    private static void syncReloadDataToPlayers(MinecraftServer server) {
        // JEI 会在收到标签和配方更新事件后重启运行时，这里保持和原版 /reload 的同步顺序一致。
        var tagsPacket = new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(server.registries()));
        var recipesPacket = new ClientboundUpdateRecipesPacket(server.getRecipeManager().getOrderedRecipes());
        for (var player : server.getPlayerList().getPlayers()) {
            player.connection.send(tagsPacket);
            player.connection.send(recipesPacket);
            player.getRecipeBook().sendInitialRecipeBook(player);
        }
    }

    private static int showStatus(CommandSourceStack source) {
        var result = RecipeOverrideManager.getLastResult();
        source.sendSuccess(() -> Component.translatable(
                "commands.viscript_recipe.reload.status",
                result.fileCount(),
                result.entryCount(),
                result.enabledEntryCount(),
                result.appliedEntryCount(),
                result.skippedEntryCount(),
                result.failedEntryCount(),
                result.baseRecipeCount(),
                result.resultRecipeCount()
        ), false);
        return 1;
    }
}
