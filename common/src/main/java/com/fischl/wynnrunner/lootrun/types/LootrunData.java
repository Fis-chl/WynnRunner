/*
 * Copyright © Wynnrunner 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner.lootrun.types;

import com.wynntils.core.WynntilsMod;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.models.lootrun.type.MissionType;
import com.wynntils.models.lootrun.type.TrialType;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class LootrunData {
    private String characterId;
    private final UUID uuid;
    private int challengesCompleted;
    private int timeElapsed;
    private LootrunLocation location;
    private Map<Integer, Challenge> challenges;
    private Map<MissionType, Boolean> missions;
    private Map<TrialType, Boolean> trials;
    private int rewardPulls;
    private int rewardRerolls;
    private int rewardSacrifices;
    private int experienceGained;
    private int mobsKilled;
    private int chestsOpened;
    private boolean failed;

    public LootrunData() {
        this.characterId = "";
        this.uuid = UUID.randomUUID();
        challenges = new TreeMap<>();
        missions = new TreeMap<>();
        trials = new TreeMap<>();
        location = LootrunLocation.UNKNOWN;
    }

    public LootrunData(
            String characterId,
            int challengesCompleted,
            int timeElapsed,
            LootrunLocation location,
            Map<Integer, Challenge> challenges,
            Map<MissionType, Boolean> missions,
            Map<TrialType, Boolean> trials,
            int rewardPulls,
            int rewardRerolls,
            int rewardSacrifices,
            int experienceGained,
            int mobsKilled,
            int chestsOpened,
            boolean failed) {
        this.characterId = characterId;
        this.uuid = UUID.randomUUID();
        this.challengesCompleted = challengesCompleted;
        this.timeElapsed = timeElapsed;
        this.location = location;
        this.challenges = new TreeMap<>(challenges);
        this.missions = new TreeMap<>(missions);
        this.trials = new TreeMap<>(trials);
        this.rewardPulls = rewardPulls;
        this.rewardRerolls = rewardRerolls;
        this.rewardSacrifices = rewardSacrifices;
        this.experienceGained = experienceGained;
        this.mobsKilled = mobsKilled;
        this.chestsOpened = chestsOpened;
        this.failed = failed;
    }

    public String getCharacterId() {
        return characterId;
    }

    public void setCharacterId(String characterId) {
        this.characterId = characterId;
    }

    public void setLocation(LootrunLocation location) {
        this.location = location;
    }

    public Challenge getChallenge(int number) {
        return challenges.computeIfAbsent(number, k -> new Challenge());
    }

    public void addChallenge(int number, Challenge challenge) {
        challenges.put(number, challenge);
    }

    public void setMissions(TreeMap<MissionType, Boolean> missions) {
        this.missions = new TreeMap<>(missions);
    }

    public void addMission(MissionType mission, boolean complete) {
        WynntilsMod.info("Adding mission " + mission.getName() + " to data");
        this.missions.put(mission, complete);
    }

    public void setTrials(TreeMap<TrialType, Boolean> trials) {
        this.trials = new TreeMap<>(trials);
    }

    public void addTrial(TrialType trial, boolean complete) {
        this.trials.put(trial, complete);
    }

    public void setChallengesCompleted(int challengesCompleted) {
        this.challengesCompleted = challengesCompleted;
    }

    public void setTimeElapsed(int timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public void setRewardPulls(int rewardPulls) {
        this.rewardPulls = rewardPulls;
    }

    public void setRewardRerolls(int rewardRerolls) {
        this.rewardRerolls = rewardRerolls;
    }

    public void setRewardSacrifices(int rewardSacrifices) {
        this.rewardSacrifices = rewardSacrifices;
    }

    public void setExperienceGained(int experienceGained) {
        this.experienceGained = experienceGained;
    }

    public void setMobsKilled(int mobsKilled) {
        this.mobsKilled = mobsKilled;
    }

    public void setChestsOpened(int chestsOpened) {
        this.chestsOpened = chestsOpened;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public UUID getUuid() {
        return uuid;
    }

    public LootrunLocation getLocation() {
        return location;
    }

    public TreeMap<MissionType, Boolean> getMissions() {
        return new TreeMap<>(missions);
    }

    public TreeMap<TrialType, Boolean> getTrials() {
        return new TreeMap<>(trials);
    }

    public int getChallengesCompleted() {
        return challengesCompleted;
    }

    public int getTimeElapsed() {
        return timeElapsed;
    }

    public int getRewardPulls() {
        return rewardPulls;
    }

    public int getRewardRerolls() {
        return rewardRerolls;
    }

    public int getRewardSacrifices() {
        return rewardSacrifices;
    }

    public int getExperienceGained() {
        return experienceGained;
    }

    public int getMobsKilled() {
        return mobsKilled;
    }

    public int getChestsOpened() {
        return chestsOpened;
    }

    public boolean getFailed() {
        return failed;
    }

    public int getNumFailedChallenges() {
        int numFailed = 0;
        for (Challenge challenge : challenges.values()) {
            if (challenge.getFailed()) {
                numFailed++;
            }
        }
        return numFailed;
    }
}
