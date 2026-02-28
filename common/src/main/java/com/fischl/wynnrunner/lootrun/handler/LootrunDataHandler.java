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
import com.wynntils.core.WynntilsMod;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.models.lootrun.type.MissionType;
import com.wynntils.models.lootrun.type.TrialType;
import com.wynntils.utils.mc.McUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;

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

    public Challenge getChallenge(int number) {
        return lootrunData.getChallenge(number);
    }

    public void addChallenge() {
        int actualChallengeNum = lootrunData.getNextChallengeNumber();
        Wynnrunner.info("Adding challenge " + actualChallengeNum);
        lootrunData.addChallenge(actualChallengeNum, currentChallenge);
        currentChallenge = new Challenge();
    }

    public void setLootrunData(LootrunData lootrunData) {
        this.lootrunData = lootrunData;
    }

    public void resetLootrunData() {
        this.lootrunData = new LootrunData();
    }

    public void setLocation(LootrunLocation location) {
        lootrunData.setLocation(location);
    }

    public void setMissions(TreeMap<MissionType, Boolean> missions) {
        lootrunData.setMissions(missions);
    }

    public void addMission(MissionType mission) {
        this.addMission(mission, false);
    }

    public void addMission(MissionType mission, boolean completed) {
        lootrunData.addMission(mission, completed);
    }

    public void setTrials(TreeMap<TrialType, Boolean> trials) {
        lootrunData.setTrials(trials);
    }

    public void addTrial(TrialType trial) {
        lootrunData.addTrial(trial, false);
    }

    public void addTrial(TrialType trial, boolean completed) {
        lootrunData.addTrial(trial, completed);
    }

    public void setChallengesCompleted(int challengesCompleted) {
        lootrunData.setChallengesCompleted(challengesCompleted);
    }

    public void setTimeElapsed(int timeElapsed) {
        lootrunData.setTimeElapsed(timeElapsed);
    }

    public void setRewardPulls(int rewardPulls) {
        lootrunData.setRewardPulls(rewardPulls);
    }

    public void setRewardRerolls(int rewardRerolls) {
        lootrunData.setRewardRerolls(rewardRerolls);
    }

    public void setRewardSacrifices(int rewardSacrifices) {
        lootrunData.setRewardSacrifices(rewardSacrifices);
    }

    public void setExperienceGained(int experienceGained) {
        lootrunData.setExperienceGained(experienceGained);
    }

    public void setMobsKilled(int mobsKilled) {
        lootrunData.setMobsKilled(mobsKilled);
    }

    public void setChestsOpened(int chestsOpened) {
        lootrunData.setChestsOpened(chestsOpened);
    }

    public void setFailed(boolean failed) {
        lootrunData.setFailed(failed);
    }

    public void loadForCharacter(String characterId) {
        // Check if there is an unfinished lootrun in progress
        Wynnrunner.info("Looking for in-progress run for character '" + characterId + "'...");
        if (lootrunDir.exists() && lootrunDir.isDirectory()) {
            File[] tmpFiles = lootrunDir.listFiles();
            assert tmpFiles != null;
            for (File tmpFile : tmpFiles) {
                if (tmpFile.isFile()
                        && tmpFile.getName().startsWith(characterId)
                        && tmpFile.getName().endsWith(".tmp")) {
                    // There can only be one in-progress lootrun for a character
                    try {
                        lootrunData =
                                new Gson().fromJson(new FileReader(tmpFile, StandardCharsets.UTF_8), LootrunData.class);
                        return;
                    } catch (IOException e) {
                        Wynnrunner.error("Unable to read in-progress lootrun data from file '"
                                + tmpFile.getAbsolutePath() + "': " + e);
                        return;
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

    public void save(boolean completed) {
        if (!getLootrunData().getCharacterId().isEmpty()) {
            File file = getFile(completed);
            Wynnrunner.info("Attempting to save lootrun " + file.getAbsolutePath());
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
                    Wynnrunner.info(gson.toJson(this.lootrunData));
                }
            } catch (JsonIOException e) {
                Wynnrunner.error("Unable to process Lootrun data for character "
                        + getLootrunData().getCharacterId());
            } catch (IOException e) {
                Wynnrunner.error("Unable to create file '" + file.getAbsolutePath() + "'");
            }
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
