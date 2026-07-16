package com.viscript_recipe.client;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.common.NeoForge;

public final class RecipeDeltaClientEvents {
    private static boolean registered;

    private RecipeDeltaClientEvents() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(
                EventPriority.HIGHEST,
                RecipesUpdatedEvent.class,
                RecipeDeltaClientState::onRecipesUpdated
        );
        NeoForge.EVENT_BUS.addListener(
                ClientPlayerNetworkEvent.LoggingOut.class,
                RecipeDeltaClientState::onClientLogout
        );
        registered = true;
    }
}
