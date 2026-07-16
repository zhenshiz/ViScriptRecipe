package com.viscript_recipe.recipe;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/** Supplies the recipe-delta baseline after vanilla finishes a player's normal login sync. */
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
