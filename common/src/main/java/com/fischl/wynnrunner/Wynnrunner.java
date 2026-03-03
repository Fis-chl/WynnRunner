/*
 * Copyright © Wynnrunner 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Wynnrunner {
    public static final String MOD_ID = "wynnrunner";
    private static String version = "";
    private static boolean developmentBuild = false;
    private static ModLoader modLoader;
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /* Init */

    public static void init(ModLoader loader, String modVersion, boolean isDevelopmentBuild) {
        // Write common init code here.
        modLoader = loader;
        version = modVersion;
        developmentBuild = isDevelopmentBuild;
        if (isDevelopmentBuild) {
            LOGGER.warn("Wynnrunner setup finished - running dev build (version {})", version);
        } else {
            LOGGER.info("Wynnrunner setup finished - running version {}", version);
        }
    }

    /* Event listener functions */

    /* Utility functions */

    public static String getVersion() {
        return version;
    }

    public static boolean isDevelopmentBuild() {
        return developmentBuild;
    }

    public static ModLoader getModLoader() {
        return modLoader;
    }

    /* Logging functions */

    public static void debug(String msg) {
        LOGGER.debug("Wynnrunner: {}", msg);
    }

    public static void info(String msg) {
        LOGGER.info("Wynnrunner: {}", msg);
    }

    public static void warn(String msg) {
        LOGGER.warn("Wynnrunner: {}", msg);
    }

    public static void error(String msg) {
        LOGGER.error("Wynnrunner: {}", msg);
    }

    public enum ModLoader {
        FORGE,
        FABRIC
    }
}
