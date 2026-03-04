/*
 * Copyright © Wynnrunner 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner.lootrun.handler;

import com.fischl.wynnrunner.Wynnrunner;
import com.fischl.wynnrunner.lootrun.types.LootrunData;
import com.google.gson.JsonObject;

public class LootrunDataUpgrader {
    public static JsonObject upgrade(JsonObject json) {
        int fileVersion = json.has("version") ? json.get("version").getAsInt() : 0;

        if (fileVersion > LootrunData.CURRENT_VERSION) {
            return json; // Or handle this separately beforehand
        }

        while (fileVersion < LootrunData.CURRENT_VERSION) {
            Wynnrunner.debug("Upgrading lootrun data from version " + fileVersion + " to " + (fileVersion + 1));
            fileVersion++;
            json.addProperty("version", fileVersion);
        }

        return json;
    }
}
