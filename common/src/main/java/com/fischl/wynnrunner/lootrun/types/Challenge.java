/*
 * Copyright © Wynnrunner 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner.lootrun.types;

import com.wynntils.models.lootrun.beacons.LootrunBeaconKind;
import com.wynntils.models.lootrun.type.TaskLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Challenge {
    private TaskLocation location;
    private Map<Integer, List<LootrunBeaconKind>> beaconsOffered;
    private LootrunBeaconKind beaconTaken;
    private boolean failed;
    private int rerolls = 0;

    public Challenge() {
        beaconsOffered = new HashMap<>();
        failed = false;
    }

    public TaskLocation getLocation() {
        return location;
    }

    public void setLocation(TaskLocation location) {
        this.location = location;
    }

    public Map<Integer, List<LootrunBeaconKind>> getBeaconsOffered() {
        return new HashMap<>(beaconsOffered);
    }

    public void setBeaconsOffered(Map<Integer, List<LootrunBeaconKind>> beaconsOffered) {
        this.beaconsOffered = new HashMap<>(beaconsOffered);
    }

    public void addBeaconOffered(LootrunBeaconKind beacon) {
        if (!beaconsOffered.get(rerolls).contains(beacon)) {
            beaconsOffered.get(rerolls).add(beacon);
        }
    }

    public void addReroll() {
        rerolls++;
    }

    public int getRerolls() {
        return rerolls;
    }

    public void setRerolls(int rerolls) {
        this.rerolls = rerolls;
    }

    public LootrunBeaconKind getBeaconTaken() {
        return beaconTaken;
    }

    public void setBeaconTaken(LootrunBeaconKind beaconTaken) {
        this.beaconTaken = beaconTaken;
    }

    public boolean getFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }
}
