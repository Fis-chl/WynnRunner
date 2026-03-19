/*
 * Copyright © Wynnrunner 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.fischl.wynnrunner.mixin;

import com.fischl.wynnrunner.Wynnrunner;
import com.fischl.wynnrunner.lootrun.handler.LootrunDataHandler;
import com.wynntils.core.components.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.handlers.particle.event.ParticleVerifiedEvent;
import com.wynntils.handlers.particle.type.ParticleType;
import com.wynntils.mc.event.ConnectionEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.extension.EntityExtension;
import com.wynntils.models.beacons.type.Beacon;
import com.wynntils.models.lootrun.LootrunModel;
import com.wynntils.models.lootrun.beacons.LootrunBeaconKind;
import com.wynntils.models.lootrun.type.LootrunLocation;
import com.wynntils.models.lootrun.type.LootrunningState;
import com.wynntils.models.lootrun.type.MissionType;
import com.wynntils.models.lootrun.type.TaskLocation;
import com.wynntils.models.lootrun.type.TaskPrediction;
import com.wynntils.models.lootrun.type.TrialType;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import com.wynntils.utils.mc.PosUtils;
import com.wynntils.utils.type.CappedValue;
import com.wynntils.utils.type.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(LootrunModel.class)
public abstract class LootrunModelMixin extends Model {
    @Unique
    private LootrunDataHandler lootrunDataHandler;

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
    @Final
    private static Pattern CHOOSE_BEACON_PATTERN;

    @Unique
    private String lastLoadedCharacterId = "";

    @Shadow
    private List<Pair<Beacon<LootrunBeaconKind>, EntityExtension>> activeBeacons;

    protected LootrunModelMixin(List<Model> dependencies) {
        super(dependencies);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    protected void LootrunModel(CallbackInfo ci) {
        lootrunDataHandler = new LootrunDataHandler();
        Wynnrunner.info("LootrunModelMixin setup successful - WynnRunner is working properly");
    }

    @Inject(method = "onLootrunParticle", at = @At("HEAD"))
    protected void onLootrunParticle(ParticleVerifiedEvent event, CallbackInfo ci) {
        if (event.getParticle().particleType() == ParticleType.LOOTRUN_TASK) {
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
                        Wynnrunner.debug("Lootrun location found - setting as '" + loc.name() + "'");
                        lootrunDataHandler.getLootrunData().setLocation(loc);
                        break;
                    }
                }
            }
        }
    }

    @Inject(method = "onWorldStateChanged", at = @At("TAIL"))
    protected void onWorldStateChanged(WorldStateEvent event, CallbackInfo ci) {
        if (event.getOldState() == WorldState.WORLD && event.getNewState() != WorldState.WORLD) {
            lootrunDataHandler.save(false);
        }
    }

    @Inject(method = "onChatMessage", at = @At("HEAD"))
    protected void onChatMessage(ChatMessageEvent.Match event, CallbackInfo ci) {
        if (event.getRecipientType() == RecipientType.INFO) {
            StyledText styledText = event.getMessage();
            if (styledText.matches(CHOOSE_BEACON_PATTERN)) {
                if (lootrunDataHandler.getCurrentChallenge().getRerolls() > 0
                                && lootrunDataHandler.getCurrentChallenge().getLocation() != null
                        || !lootrunDataHandler
                                .getCurrentChallenge()
                                .getBeaconsOffered()
                                .isEmpty()) {
                    lootrunDataHandler.getCurrentChallenge().addReroll();
                }
                for (Pair<Beacon<LootrunBeaconKind>, EntityExtension> beaconPair : activeBeacons) {
                    lootrunDataHandler
                            .getCurrentChallenge()
                            .addBeaconOffered(beaconPair.a().beaconKind());
                }
            }
        }
    }

    @Inject(method = "addMission", at = @At("HEAD"))
    protected void addMission(MissionType mission, CallbackInfo ci) {
        lootrunDataHandler.getLootrunData().addMission(mission);
    }

    @Inject(method = "addTrial", at = @At("HEAD"))
    protected void addTrial(TrialType trial, CallbackInfo ci) {
        lootrunDataHandler.getLootrunData().addTrial(trial);
    }

    @Inject(method = "handleStateChange", at = @At("HEAD"))
    protected void handleStateChange(LootrunningState oldState, LootrunningState newState, CallbackInfo ci) {
        if (newState == LootrunningState.NOT_RUNNING) {
            lootrunDataHandler.save(false);
            return;
        }
        // Sometimes the LootrunBeaconSelected event does not get fired
        // If we were to subscribe to this event in a method we could possibly miss out on both the beacon taken,
        // and its location.
        // This section is a workaround for the issue
        Beacon closestBeacon = getClosestBeacon();
        if (oldState == LootrunningState.CHOOSING_BEACON
                && newState == LootrunningState.IN_TASK
                && closestBeacon != null
                && closestBeacon.beaconKind() instanceof LootrunBeaconKind color) {
            // Beacon taken should be fine here
            lootrunDataHandler.getCurrentChallenge().setBeaconTaken(color);
            // beacons may not contain an entry for the beacon color
            var prediction = beacons.get(color);
            if (prediction != null && prediction.taskLocation() != null) {
                lootrunDataHandler.getCurrentChallenge().setLocation(prediction.taskLocation());
            } else {
                Wynnrunner.warn("Unable to determine task location for challenge #"
                        + lootrunDataHandler.getLootrunData().getNextChallengeNumber()
                        + " estimating based on position...");
                Vec3 playerPos = McUtils.player().position();
                if (lootrunDataHandler.getLootrunData().getLocation() == LootrunLocation.UNKNOWN) {
                    Wynnrunner.error("Unable to determine lootrun location for challenge #"
                            + lootrunDataHandler.getLootrunData().getNextChallengeNumber()
                            + " unable to estimate task location");
                    return;
                }
                TaskLocation bestLocation = getBestTaskLocation(
                        playerPos, lootrunDataHandler.getLootrunData().getLocation());
                if (bestLocation != null) {
                    lootrunDataHandler.getCurrentChallenge().setLocation(bestLocation);
                    Wynnrunner.info("Estimated task location for challenge #"
                            + lootrunDataHandler.getLootrunData().getNextChallengeNumber()
                            + " as " + bestLocation.name());
                } else {
                    Wynnrunner.error("Unable to determine task location for challenge #"
                            + lootrunDataHandler.getLootrunData().getNextChallengeNumber()
                            + " unable to estimate task location");
                }
            }
        }
    }

    @Unique
    protected TaskLocation getBestTaskLocation(Vec3 playerPos, LootrunLocation location) {
        TaskLocation bestLocation = null;
        double bestDistanceSq = Double.POSITIVE_INFINITY;
        for (TaskLocation loc : taskLocations.get(location)) {
            Vec3 locPos = loc.location().toVec3();
            double dx = playerPos.x - locPos.x;
            double dz = playerPos.z - locPos.z;
            double distanceSq = dx * dx + dz * dz;
            if (bestLocation == null || distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                bestLocation = loc;
            }
        }
        return bestLocation;
    }

    @Inject(method = "challengeCompleted", at = @At("TAIL"))
    protected void challengeCompleted(CallbackInfo ci) {
        lootrunDataHandler.addChallenge();
    }

    @Inject(method = "challengeFailed", at = @At("TAIL"))
    protected void challengeFailed(CallbackInfo ci) {
        lootrunDataHandler.getCurrentChallenge().setFailed(true);
        lootrunDataHandler.addChallenge();
    }

    @Inject(method = "parseCompletedMessages", at = @At("HEAD"))
    protected void parseCompletedMessages(StyledText styledText, CallbackInfo ci) {
        Matcher matcher = styledText.getMatcher(REWARD_PULLS_PATTERN);
        if (matcher.find()) {
            int pulls = Integer.parseInt(matcher.group(1));
            lootrunDataHandler.getLootrunData().setRewardPulls(pulls);
        }

        matcher = styledText.getMatcher(TIME_ELAPSED_PATTERN);
        if (matcher.find()) {
            lootrunDataHandler
                    .getLootrunData()
                    .setTimeElapsed(Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2)));
        }

        matcher = styledText.getMatcher(REWARD_SACRIFICES_PATTERN);
        if (matcher.find()) {
            lootrunDataHandler.getLootrunData().setRewardSacrifices(Integer.parseInt(matcher.group(1)));

            matcher = styledText.getMatcher(CHESTS_OPENED_PATTERN);
            if (matcher.find()) {
                lootrunDataHandler.getLootrunData().setChestsOpened(Integer.parseInt(matcher.group(1)));
            }
        }

        matcher = styledText.getMatcher(LOOTRUN_EXPERIENCE_PATTERN);
        if (matcher.find()) {
            lootrunDataHandler.getLootrunData().setExperienceGained(Integer.parseInt(matcher.group(1)));

            matcher = styledText.getMatcher(CHALLENGES_COMPLETED_PATTERN);
            if (matcher.find()) {
                lootrunDataHandler.getLootrunData().setChallengesCompleted(Integer.parseInt(matcher.group(1)));
                lootrunDataHandler.save(true);
                lootrunDataHandler.resetLootrunData();
                lastLoadedCharacterId = "";
            }
        }
    }

    @Inject(method = "parseFailedMessages", at = @At("HEAD"))
    protected void parseFailedMessages(StyledText styledText, CallbackInfo ci) {
        Matcher matcher = styledText.getMatcher(CHALLENGES_COMPLETED_PATTERN);
        if (matcher.find()) {
            lootrunDataHandler.getLootrunData().setFailed(true);
            lootrunDataHandler.save(true);
            lootrunDataHandler.resetLootrunData();
            lastLoadedCharacterId = "";
        }
    }

    @Unique
    @SubscribeEvent
    public void onDisconnectedEvent(ConnectionEvent.DisconnectedEvent event) {
        lootrunDataHandler.save(false);
        lootrunDataHandler.resetLootrunData();
        lastLoadedCharacterId = "";
    }

    @Unique
    @SubscribeEvent
    public void onClientTick(TickEvent event) {
        try {
            if (Models.WorldState.getCurrentState() != WorldState.WORLD) return;
            String id = Models.Character.getId();
            if (id == null || id.isEmpty()) return;
            if (id.equals(lastLoadedCharacterId)) return;
            Wynnrunner.info("Detected character change on tick, loading lootrun for " + id);
            lootrunDataHandler.loadForCharacter(id);
            if (lootrunDataHandler.getLootrunData().getCharacterId().isEmpty()) {
                lootrunDataHandler.getLootrunData().setCharacterId(id);
            }
            lastLoadedCharacterId = id;
        } catch (Throwable t) {
            Wynnrunner.error("Error in lootrun tick poller: " + t);
        }
    }
}
