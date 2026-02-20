package com.fischl.wynnrunner.neoforge;

import com.fischl.wynnrunner.Wynnrunner;
import net.neoforged.fml.common.Mod;

@Mod(Wynnrunner.MOD_ID)
public final class WynnrunnerNeoForge {
    public WynnrunnerNeoForge() {
        // Run our common setup.
        Wynnrunner.init();
    }
}
