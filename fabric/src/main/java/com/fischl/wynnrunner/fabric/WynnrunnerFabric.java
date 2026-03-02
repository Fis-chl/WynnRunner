/*
 * Copyright © Wynnrunner 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner.fabric;

import com.fischl.wynnrunner.Wynnrunner;
import java.util.Optional;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public final class WynnrunnerFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Optional<ModContainer> wynnrunnerMod = FabricLoader.getInstance().getModContainer("wynnrunner");
        if (wynnrunnerMod.isEmpty()) {
            Wynnrunner.error("Wynnrunner not found :(");
            return;
        }

        Wynnrunner.init(
                Wynnrunner.ModLoader.FABRIC,
                wynnrunnerMod.get().getMetadata().getVersion().getFriendlyString(),
                FabricLoader.getInstance().isDevelopmentEnvironment());
    }
}
