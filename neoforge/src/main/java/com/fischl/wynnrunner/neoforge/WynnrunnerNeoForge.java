/*
 * Copyright © Wynnrunner 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner.neoforge;

import com.fischl.wynnrunner.Wynnrunner;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Wynnrunner.MOD_ID)
public final class WynnrunnerNeoForge {
    public WynnrunnerNeoForge() {
        Wynnrunner.init(
                Wynnrunner.ModLoader.FORGE,
                ModLoadingContext.get()
                        .getActiveContainer()
                        .getModInfo()
                        .getVersion()
                        .toString(),
                !FMLEnvironment.isProduction());
    }
}
