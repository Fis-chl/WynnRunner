/*
 * Copyright © Wynnrunner 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner.mixin;

import com.fischl.wynnrunner.Wynnrunner;
import com.fischl.wynnrunner.lootrun.handler.LootrunDataHandler;
import com.fischl.wynnrunner.lootrun.types.Challenge;
import com.wynntils.core.components.Model;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.particle.event.ParticleVerifiedEvent;
import com.wynntils.handlers.particle.type.ParticleType;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.models.beacons.event.BeaconMarkerEvent;
import com.wynntils.models.beacons.type.Beacon;
import com.wynntils.models.beacons.type.BeaconMarker;
import com.wynntils.models.lootrun.LootrunModel;
import com.wynntils.models.lootrun.beacons.LootrunBeaconKind;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.models.lootrun.type.LootrunningState;
import com.wynntils.models.lootrun.type.MissionType;
import com.wynntils.models.lootrun.type.TaskLocation;
import com.wynntils.models.lootrun.type.TaskPrediction;
import com.wynntils.models.lootrun.type.TrialType;
import com.wynntils.utils.mc.PosUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootrunModel.class)
public abstract class LootrunModelMixin extends Model {
    // TODO load an in-progress lootrun
    private LootrunDataHandler lootrunDataHandler;
    private Challenge currentChallengeData;

    // Shadows
    @Shadow
    private CappedValue challenges;

    @Shadow
    private Map<LootrunLocation, Set<TaskLocation>> taskLocations;

    @Shadow
    private Map<LootrunBeaconKind, TaskPrediction> beacons;

    @Shadow
    private Set<TaskLocation> possibleTaskLocations;

    @Final
    @Shadow
    private static int TASK_POSITION_ERROR;

    @Shadow
    public abstract Beacon getClosestBeacon();

    @Shadow
    @Final
    private static Pattern REWARD_PULLS_PATTERN;

    @Shadow
    @Final
    private static Pattern TIME_ELAPSED_PATTERN;

    @Shadow
    @Final
    private static Pattern REWARD_SACRIFICES_PATTERN;

    @Shadow
    @Final
    private static Pattern CHESTS_OPENED_PATTERN;

    @Shadow
    @Final
    private static Pattern LOOTRUN_EXPERIENCE_PATTERN;

    @Shadow
    @Final
    private static Pattern CHALLENGES_COMPLETED_PATTERN;

    @Shadow
    private List<Pair<Beacon<LootrunBeaconKind>, EntityExtension>> activeBeacons;

    protected LootrunModelMixin(List<Model> dependencies) {
        super(dependencies);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    protected void LootrunModel(CallbackInfo ci) {
        lootrunDataHandler = new LootrunDataHandler();
        currentChallengeData = new Challenge();
        Wynnrunner.info("LootrunModelMixin setup successful - WynnRunner is working properly");
    }

    @Inject(method = "onLootrunParticle", at = @At("HEAD"))
    protected void onLootrunParticle(ParticleVerifiedEvent event, CallbackInfo ci) {
        if (event.getParticle().particleType() != ParticleType.LOOTRUN_TASK) return;
        // Run through the task location entry set and get the lootrun location, only if the location is currently
        // unknown
        if (lootrunDataHandler.getLootrunData().getLocation() == LootrunLocation.UNKNOWN) {
            LootrunLocation loc = LootrunLocation.UNKNOWN;
            boolean foundTaskLocation = false;
            for (Map.Entry<LootrunLocation, Set<TaskLocation>> entry : this.taskLocations.entrySet()) {
                for (TaskLocation taskLocation : entry.getValue()) {
                    if (PosUtils.closerThanIgnoringY(
                            taskLocation.location().toVec3(),
                            event.getParticle().position(),
                            TASK_POSITION_ERROR)) {
                        foundTaskLocation = true;
                        loc = entry.getKey();
                        break;
                    }
                }
                if (foundTaskLocation) {
                    Wynnrunner.info("Lootrun location found - setting as '" + loc.name() + "'");
                    lootrunDataHandler.setLocation(loc);
                    break;
                }
            }
        }
    }

    @Inject(method = "onBeaconMarkerAdded", at = @At("TAIL"))
    protected void onBeaconMarkerAdded(BeaconMarkerEvent.Added event, CallbackInfo ci) {
        BeaconMarker beaconMarker = event.getBeaconMarker();
        Pair<Beacon<LootrunBeaconKind>, EntityExtension> beaconPair = null;
        for (Pair<Beacon<LootrunBeaconKind>, EntityExtension> activeBeacon : activeBeacons) {
            if (activeBeacon
                    .a()
                    .beaconKind()
                    .getCustomColor()
                    .equals(beaconMarker.color().get())) {
                beaconPair = activeBeacon;
                break;
            }
        }
        if (beaconPair != null) {
            currentChallengeData.addBeaconOffered(beaconPair.a().beaconKind());
        }
    }

    @Inject(method = "addMission", at = @At("HEAD"))
    protected void addMission(MissionType mission, CallbackInfo ci) {
        lootrunDataHandler.addMission(mission);
    }

    @Inject(method = "addTrial", at = @At("HEAD"))
    protected void addTrial(TrialType trial, CallbackInfo ci) {
        lootrunDataHandler.addTrial(trial);
    }

    @Inject(method = "handleStateChange", at = @At("HEAD"))
    protected void handleStateChange(LootrunningState oldState, LootrunningState newState, CallbackInfo ci) {
        if (newState == LootrunningState.NOT_RUNNING) {
            return;
        }

        Beacon closestBeacon = getClosestBeacon();
        if (oldState == LootrunningState.CHOOSING_BEACON
                && newState == LootrunningState.IN_TASK
                && closestBeacon != null
                && closestBeacon.beaconKind() instanceof LootrunBeaconKind color) {
            currentChallengeData.setBeaconTaken(color);
            var prediction = beacons.get(closestBeacon.beaconKind());
            if (prediction != null) {
                currentChallengeData.setLocation(prediction.taskLocation());
            }
        }
    }

    @Inject(method = "challengeCompleted", at = @At("TAIL"))
    protected void challengeCompleted(CallbackInfo ci) {
        lootrunDataHandler.addChallenge(challenges.current(), currentChallengeData);
        currentChallengeData = new Challenge();
    }

    @Inject(method = "parseCompletedMessages", at = @At("HEAD"))
    protected void parseCompletedMessages(StyledText styledText, CallbackInfo ci) {
        Matcher matcher = styledText.getMatcher(REWARD_PULLS_PATTERN);
        if (matcher.find()) {
            int pulls = Integer.parseInt(matcher.group(1));
            lootrunDataHandler.setRewardPulls(pulls);
        }

        matcher = styledText.getMatcher(TIME_ELAPSED_PATTERN);
        if (matcher.find()) {
            lootrunDataHandler.setTimeElapsed(
                    Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2)));
        }

        matcher = styledText.getMatcher(REWARD_SACRIFICES_PATTERN);
        if (matcher.find()) {
            lootrunDataHandler.setRewardSacrifices(Integer.parseInt(matcher.group(1)));

            matcher = styledText.getMatcher(CHESTS_OPENED_PATTERN);
            if (matcher.find()) {
                lootrunDataHandler.setChestsOpened(Integer.parseInt(matcher.group(1)));
            }
        }

        matcher = styledText.getMatcher(LOOTRUN_EXPERIENCE_PATTERN);
        if (matcher.find()) {
            lootrunDataHandler.setExperienceGained(Integer.parseInt(matcher.group(1)));

            matcher = styledText.getMatcher(CHALLENGES_COMPLETED_PATTERN);
            if (matcher.find()) {
                lootrunDataHandler.setChallengesCompleted(Integer.parseInt(matcher.group(1)));
                lootrunDataHandler.process();
                lootrunDataHandler.resetLootrunData();
            }
        }
    }

    @Inject(method = "parseFailedMessages", at = @At("HEAD"))
    protected void parseFailedMessages(StyledText styledText, CallbackInfo ci) {
        Matcher matcher = styledText.getMatcher(CHALLENGES_COMPLETED_PATTERN);
        if (matcher.find()) {
            lootrunDataHandler.setFailed(true);
            lootrunDataHandler.process();
            lootrunDataHandler.resetLootrunData();
        }
    }
}
