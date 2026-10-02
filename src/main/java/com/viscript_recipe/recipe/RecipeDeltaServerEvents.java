package com.viscript_recipe.recipe;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** 等原版玩家登录同步完成后，再发送配方增量同步的基线。 */
public final class RecipeDeltaServerEvents {
    private static boolean registered;

    private RecipeDeltaServerEvents() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(
                PlayerEvent.PlayerLoggedInEvent.class,
                RecipeDeltaServerEvents::onPlayerLoggedIn
        );
        registered = true;
    }

    private static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            RecipeReloadSyncService.syncBaselineToPlayer(player);
        }
    }
}
