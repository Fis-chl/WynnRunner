/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner.lootrun.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.wynntils.core.WynntilsMod;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.models.lootrun.type.MissionType;
import com.wynntils.models.lootrun.type.TrialType;
import com.wynntils.utils.mc.McUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import com.fischl.wynnrunner.lootrun.types.LootrunData;
import com.fischl.wynnrunner.lootrun.types.Challenge;

public class LootrunDataHandler {
    private LootrunData lootrunData;
    private File outputFile;
    private int challengeNumber;

    public LootrunDataHandler() {
        // Create blank lootrunData
        this.lootrunData = new LootrunData();
        // Store in '.minecraft/wynntils/lrdata/{uuid}.json'
        File parentDir = McUtils.getGameDirectory();
        File output = new File(parentDir, "wynntils");
        File lootrun = new File(output, "lrdata");
        if (!lootrun.exists() && !lootrun.mkdirs()) {
            WynntilsMod.error("Unable to make directory '" + lootrun.getAbsolutePath() + "' for lootrun data.");
        } else {
            outputFile = new File(lootrun, lootrunData.getUuid() + ".json");
        }
    }

    public LootrunData getLootrunData() {
        return lootrunData;
    }

    public Challenge getChallenge(int number) {
        return lootrunData.getChallenge(number);
    }

    public void addChallenge(int number, Challenge challenge) {
        lootrunData.addChallenge(number, challenge);
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

    public void process() {
        WynntilsMod.info("LOOTRUN COMPLETE");
        if (outputFile != null) {
            try {
                WynntilsMod.info("Processing LootrunData into JSON at file '" + outputFile.getAbsolutePath() + "'");
                if (outputFile.exists() || outputFile.createNewFile()) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    try (FileWriter fw = new FileWriter(outputFile, StandardCharsets.UTF_8)) {
                        gson.toJson(this.lootrunData, fw);
                    }
                    WynntilsMod.info("PROCESSED");
                } else {
                    WynntilsMod.error("Unable to create file '" + outputFile.getAbsolutePath() + "'");
                }
            } catch (IOException e) {
                WynntilsMod.error("Unable to create file '" + outputFile.getAbsolutePath()
                        + "' - parent directory does not exist");
            } catch (JsonIOException e) {
                WynntilsMod.error("Unable to process LootrunData ");
            }
        } else {
            WynntilsMod.info("File for lootrun data is null - skipping write...");
        }
    }
}
