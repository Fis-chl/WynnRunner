package com.fischl.wynnrunner.fabric;

import com.fischl.wynnrunner.Wynnrunner;
import net.fabricmc.api.ClientModInitializer;

public final class WynnrunnerFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Wynnrunner.init();
    }
}
