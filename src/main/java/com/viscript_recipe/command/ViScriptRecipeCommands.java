package com.viscript_recipe.command;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.viscript_lib.gui.editor.EditorAssetFiles;
import com.viscript_lib.register.ICommand;
import com.viscript_recipe.Config;
import com.viscript_recipe.ViScriptRecipe;
import com.viscript_recipe.data.RecipeDataAccessors;
import com.viscript_recipe.data.RecipeFile;
import com.viscript_recipe.gui.editor.RecipeEditor;
import com.viscript_recipe.gui.editor.RecipeProjectType;
import com.viscript_recipe.network.s2c.JeiShowcaseS2CPayload;
import com.viscript_recipe.network.s2c.RecipeEditorS2CPayload;
import com.viscript_recipe.recipe.RecipeOverrideManager;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.network.connection.ConnectionType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@LDLRegister(registry = ICommand.COMMAND_ID, name = "recipe")
public class ViScriptRecipeCommands implements ICommand {

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        dispatcher.register(Commands.literal(ViScriptRecipe.MOD_ID)
                .requires(source -> source.hasPermission(4))
                .then(Commands.literal("editor")
                        .executes(context -> openEditor(context.getSource(), null))
                        .then(Commands.argument("file", StringArgumentType.greedyString())
                                .suggests(ViScriptRecipeCommands::suggestRecipeFiles)
                                .executes(context -> openEditor(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "file")
                                ))))
                .then(Commands.literal("reload")
                        .executes(context -> reloadRecipes(context.getSource())))
                .then(Commands.literal("status")
                        .executes(context -> showStatus(context.getSource()))));
    }

    private static int openEditor(CommandSourceStack source, String filePath) {
        var server = source.getServer();
        var player = source.getPlayer();
        if (player == null) {
            return 0;
        }
        if (!server.isSingleplayer()) {
            source.sendFailure(Component.translatable("commands.viscript_recipe.editor.singleplayer_only"));
            return 0;
        }
        if (!PlayerUIMenuType.openUI(player, RecipeEditor.WINDOW_ID)) {
            return 0;
        }
        if (filePath == null || filePath.isBlank()) {
            return 1;
        }
        var cleanFilePath = cleanFilePathArgument(filePath);
        if (cleanFilePath.isBlank()) {
            return 1;
        }

        Path target;
        try {
            target = EditorAssetFiles.resolveRuntimeFile(RecipeProjectType.FORMAT, cleanFilePath, true);
        } catch (IllegalArgumentException e) {
            source.sendFailure(Component.translatable("commands.viscript_recipe.editor.invalid_path", filePath));
            return 0;
        }

        var root = RecipeProjectType.FORMAT.functionDirectory().toPath().toAbsolutePath().normalize();
        var relativePath = normalize(root.relativize(target));
        try {
            var recipeFile = loadRecipeFile(target, server);
            RPCPacketDistributor.rpcToPlayer(
                    player,
                    RecipeEditorS2CPayload.OPEN_RECIPE_EDITOR_FILE,
                    relativePath,
                    recipeFile.serializeNBT(server.registryAccess())
            );
            source.sendSuccess(() -> Component.translatable(
                    Files.exists(target)
                            ? "commands.viscript_recipe.editor.opened"
                            : "commands.viscript_recipe.editor.opened_new",
                    relativePath
            ), false);
            return 1;
        } catch (Exception e) {
            ViScriptRecipe.LOGGER.error("Failed to open recipe editor file {}", target, e);
            source.sendFailure(Component.translatable("commands.viscript_recipe.editor.load_failed", relativePath));
            return 0;
        }
    }

    private static CompletableFuture<Suggestions> suggestRecipeFiles(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder) {
        for (var file : EditorAssetFiles.listRuntimeFiles(RecipeProjectType.FORMAT, true)) {
            builder.suggest(file);
        }
        return builder.buildFuture();
    }

    private static String cleanFilePathArgument(String filePath) {
        var path = filePath == null ? "" : filePath.trim();
        if (path.length() >= 2) {
            var first = path.charAt(0);
            var last = path.charAt(path.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return path.substring(1, path.length() - 1).trim();
            }
        }
        return path;
    }

    private static RecipeFile loadRecipeFile(Path path, MinecraftServer server) throws IOException {
        RecipeDataAccessors.register();
        if (!Files.exists(path)) {
            return new RecipeFile();
        }
        CompoundTag tag;
        try {
            tag = NbtIo.read(path);
        } catch (IOException rawReadError) {
            try {
                tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            } catch (Exception compressedReadError) {
                throw rawReadError;
            }
        }
        if (tag == null) {
            try {
                tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap());
            } catch (Exception ignored) {
                tag = null;
            }
        }
        if (tag == null) {
            return new RecipeFile();
        }
        var recipeFile = new RecipeFile();
        recipeFile.deserializeNBT(server.registryAccess(), tag);
        return recipeFile;
    }

    private static String normalize(Path path) {
        return path.toString().replace('\\', '/');
    }

    private static int reloadRecipes(CommandSourceStack source) {
        var server = source.getServer();
        var recipePayloadHashBeforeReload = server.getPlayerList().getPlayers().isEmpty()
                ? null
                : createRecipePayloadHash(server);
        var result = RecipeOverrideManager.reload(server.getRecipeManager(), server.registryAccess());
        syncReloadDataToPlayers(server, recipePayloadHashBeforeReload);
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

    private static void syncReloadDataToPlayers(MinecraftServer server, @Nullable byte[] recipePayloadHashBeforeReload) {
        var players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }

        var recipePayloadHashAfterReload = createRecipePayloadHash(server);
        var recipePayloadChanged = recipePayloadHashBeforeReload == null
                || recipePayloadHashAfterReload == null
                || !Arrays.equals(recipePayloadHashBeforeReload, recipePayloadHashAfterReload);
        // 本命令只重读 .recipe；发送 UpdateTags 会让 ALI 清空并重新请求 loot 数据。
        var recipesPacket = recipePayloadChanged
                ? new ClientboundUpdateRecipesPacket(server.getRecipeManager().getOrderedRecipes())
                : null;
        var showcaseOnly = Config.SHOWCASE_ONLY_VISCRIPT_RECIPES.get();
        for (var player : players) {
            if (recipesPacket != null) {
                player.connection.send(recipesPacket);
                player.getRecipeBook().sendInitialRecipeBook(player);
            }
            RPCPacketDistributor.rpcToPlayer(player, JeiShowcaseS2CPayload.SYNC_SHOWCASE_MODE, showcaseOnly);
        }
    }

    @Nullable
    private static byte[] createRecipePayloadHash(MinecraftServer server) {
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), server.registryAccess(), ConnectionType.NEOFORGE);
        try {
            ClientboundUpdateRecipesPacket.STREAM_CODEC.encode(
                    buffer,
                    new ClientboundUpdateRecipesPacket(server.getRecipeManager().getOrderedRecipes())
            );
            var bytes = new byte[buffer.readableBytes()];
            buffer.getBytes(buffer.readerIndex(), bytes);
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (Exception e) {
            ViScriptRecipe.LOGGER.warn("Failed to fingerprint client recipe payload; forcing recipe sync", e);
            return null;
        } finally {
            buffer.release();
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
