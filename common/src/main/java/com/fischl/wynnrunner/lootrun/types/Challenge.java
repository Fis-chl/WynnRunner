/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner.lootrun.types;

import com.wynntils.models.lootrun.beacons.LootrunBeaconKind;
import com.wynntils.models.lootrun.type.TaskLocation;
import java.util.ArrayList;
import java.util.List;

public class Challenge {
    private TaskLocation location;
    private List<LootrunBeaconKind> beaconsOffered;
    private LootrunBeaconKind beaconTaken;

    public Challenge() {
        beaconsOffered = new ArrayList<>();
    }

    public TaskLocation getLocation() {
        return location;
    }

    public void setLocation(TaskLocation location) {
        this.location = location;
    }

    public List<LootrunBeaconKind> getBeaconsOffered() {
        return new ArrayList<>(beaconsOffered);
    }

    public void setBeaconsOffered(List<LootrunBeaconKind> beaconsOffered) {
        this.beaconsOffered = new ArrayList<>(beaconsOffered);
    }

    public void addBeaconOffered(LootrunBeaconKind beacon) {
        if (!beaconsOffered.contains(beacon)) {
            beaconsOffered.add(beacon);
        }
    }

    public LootrunBeaconKind getBeaconTaken() {
        return beaconTaken;
    }

    public void setBeaconTaken(LootrunBeaconKind beaconTaken) {
        this.beaconTaken = beaconTaken;
    }
}
