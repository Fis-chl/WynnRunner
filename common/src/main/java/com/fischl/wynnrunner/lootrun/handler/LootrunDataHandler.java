/*
 * Copyright © Wynnrunner 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner.lootrun.handler;

import com.fischl.wynnrunner.Wynnrunner;
import com.fischl.wynnrunner.lootrun.types.Challenge;
import com.fischl.wynnrunner.lootrun.types.LootrunData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wynntils.core.WynntilsMod;
import com.wynntils.utils.mc.McUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LootrunDataHandler {
    private LootrunData lootrunData;
    private final File lootrunDir;
    private Challenge currentChallenge;

    public LootrunDataHandler() {
        File parentDir = McUtils.getGameDirectory();
        File output = new File(parentDir, "wynnrunner");
        lootrunDir = new File(output, "lrdata");
        if (!lootrunDir.exists() && !lootrunDir.mkdirs()) {
            WynntilsMod.error("Unable to make directory '" + lootrunDir.getAbsolutePath() + "' for lootrun data.");
        }
        // Don't load the data yet, but do create some empty lootrun data
        lootrunData = new LootrunData();
        currentChallenge = new Challenge();
    }

    public Challenge getCurrentChallenge() {
        return currentChallenge;
    }

    public void setCurrentChallenge(Challenge challenge) {
        this.currentChallenge = challenge;
    }

    public LootrunData getLootrunData() {
        return lootrunData;
    }

    public void addChallenge() {
        int actualChallengeNum = lootrunData.getNextChallengeNumber();
        Wynnrunner.debug("Adding challenge " + actualChallengeNum);
        lootrunData.addChallenge(actualChallengeNum, currentChallenge);
        currentChallenge = new Challenge();
    }

    public void setLootrunData(LootrunData lootrunData) {
        this.lootrunData = lootrunData;
    }

    public void resetLootrunData() {
        this.lootrunData = new LootrunData();
    }

    public void loadForCharacter(String characterId) {
        // Check if there is an unfinished lootrun in progress
        Wynnrunner.debug("Looking for in-progress run for character '" + characterId + "'...");
        if (lootrunDir.exists() && lootrunDir.isDirectory()) {
            File[] tmpFiles = lootrunDir.listFiles();
            assert tmpFiles != null;
            for (File tmpFile : tmpFiles) {
                if (tmpFile.isFile()
                        && tmpFile.getName().startsWith(characterId)
                        && tmpFile.getName().endsWith(".tmp")) {
                    // There can only be one in-progress lootrun for a character
                    try {
                        JsonObject json = JsonParser.parseReader(new FileReader(tmpFile, StandardCharsets.UTF_8))
                                .getAsJsonObject();
                        int fileVersion =
                                json.has("version") ? json.get("version").getAsInt() : 0;

                        if (fileVersion > LootrunData.CURRENT_VERSION) {
                            Wynnrunner.error("Lootrun data format is newer than expected (File: " + fileVersion
                                    + ", Expected: " + LootrunData.CURRENT_VERSION + "). Creating new data.");
                            saveAsIs(tmpFile);
                            break;
                        }

                        if (fileVersion < LootrunData.CURRENT_VERSION) {
                            json = LootrunDataUpgrader.upgrade(json);
                        }

                        lootrunData = new Gson().fromJson(json, LootrunData.class);
                        return;
                    } catch (Exception e) {
                        Wynnrunner.error("Unable to read or upgrade in-progress lootrun data from file '"
                                + tmpFile.getAbsolutePath() + "': " + e);
                        saveAsIs(tmpFile);
                        break;
                    }
                }
            }
            // If no data exists then just make a new one
            lootrunData = new LootrunData();
            lootrunData.setCharacterId(characterId);
        } else {
            Wynnrunner.error("Unable to find directory '" + lootrunDir.getAbsolutePath() + "'");
        }
    }

    private void saveAsIs(File tmpFile) {
        File backupFile = new File(tmpFile.getParentFile(), tmpFile.getName() + ".bak");
        if (tmpFile.renameTo(backupFile)) {
            Wynnrunner.error("Saved invalid/newer lootrun data as: " + backupFile.getName());
        } else {
            Wynnrunner.error("Failed to backup invalid/newer lootrun data: " + tmpFile.getName());
        }
    }

    public void save(boolean completed) {
        if (getLootrunData().getCharacterId().isEmpty()) return;
        File file = getFile(completed);
        Wynnrunner.debug("Attempting to save lootrun " + file.getAbsolutePath());
        try {
            if (file.exists() || file.createNewFile()) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try (java.io.OutputStreamWriter fw = new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(file, false), java.nio.charset.StandardCharsets.UTF_8)) {
                    gson.toJson(this.lootrunData, fw);
                }
                if (completed) {
                    // Need to delete
                    File tmpFile = getFile(false);
                    if (!tmpFile.delete()) {
                        Wynnrunner.error("Unable to delete temp file " + tmpFile.getName());
                    }
                }
                Wynnrunner.debug("Outputting the following JSON to file: " + gson.toJson(this.lootrunData));
            }
        } catch (JsonIOException e) {
            Wynnrunner.error("Unable to process Lootrun data for character "
                    + getLootrunData().getCharacterId());
        } catch (IOException e) {
            Wynnrunner.error("Unable to create file '" + file.getAbsolutePath() + "'");
        }
    }

    private File getFile(boolean completed) {
        File file;
        if (completed) {
            file = new File(
                    lootrunDir,
                    getLootrunData().getCharacterId() + "-" + getLootrunData().getUuid() + ".json");
        } else {
            // Only save data that has a character ID - this assumes it is an actual run
            // Save to ${lootrunDir}/${characterId}-${uuid}.tmp
            file = new File(
                    lootrunDir,
                    getLootrunData().getCharacterId() + "-" + getLootrunData().getUuid() + ".tmp");
        }
        return file;
    }
}
