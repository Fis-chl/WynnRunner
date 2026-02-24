/*
 * Copyright © Wynnrunner 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Wynnrunner {
    public static final String MOD_ID = "wynnrunner";

    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /* Init */

    public static void init() {
        // Write common init code here.
        LOGGER.info("Setup for Wynnrunner started...");
    }

    /* Logging functions */

    public static void debug(String msg) {
        LOGGER.debug(msg);
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    public static void warn(String msg) {
        LOGGER.warn(msg);
    }

    public static void error(String msg) {
        LOGGER.error(msg);
    }
}
