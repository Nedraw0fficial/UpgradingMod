package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StepAssistDisk extends UpgradeDisk {
    private static final ResourceLocation STEP_HEIGHT_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "step_assist_height");
    private static final ResourceLocation SWIM_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "step_assist_swim_speed");
    private static final ResourceLocation MOVEMENT_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "step_assist_movement_speed");

    // Track which level is currently applied to each player
    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    public StepAssistDisk() {
        super("step_assist", "Step Assist", DiskRarity.RARE);

        this.withDescription(4, "+0.1 blocks of auto-step up but\n-32% swim speed")
                .withDescription(5, "+0.2 blocks of auto-step up but\n-28% swim speed")
                .withDescription(6, "+0.3 blocks of auto-step up but\n-24% swim speed")
                .withDescription(7, "+0.4 blocks of auto-step up but\n-20% swim speed")
                .withDescription(8, "+0.5 blocks of auto-step up but\n-16% swim speed")
                .withDescription(9, "+0.6 blocks of auto-step up but\n-12% swim speed")
                .withDescription(10, "+0.7 blocks of auto-step up but\n-8% swim speed")
                .withDescription(11, "+0.8 blocks of auto-step up but\n-4% swim speed")
                .withDescription(12, "+1.2 blocks of auto-step up\nand +5.5% movement speed");
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        // Only update attributes if level changed
        if (appliedLevel == null || appliedLevel != level) {
            var stepHeightAttribute = player.getAttribute(Attributes.STEP_HEIGHT);
            var swimSpeedAttribute = player.getAttribute(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED);
            var movementSpeedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

            if (level < 12) {
                // Levels 4-11: Step height bonus + swim speed penalty
                if (stepHeightAttribute != null) {
                    stepHeightAttribute.removeModifier(STEP_HEIGHT_MODIFIER_ID);

                    // Calculate step height: (level - 3) * 0.1
                    double stepBonus = (level - 3) * 0.1;
                    AttributeModifier stepModifier = new AttributeModifier(
                            STEP_HEIGHT_MODIFIER_ID,
                            stepBonus,
                            AttributeModifier.Operation.ADD_VALUE
                    );
                    stepHeightAttribute.addPermanentModifier(stepModifier);
                }

                // Swim speed penalty
                if (swimSpeedAttribute != null) {
                    swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);

                    // Calculate swim penalty: (level - 3) * 4%
                    double swimPenalty = -((12 - level) * 4) / 100.0;
                    AttributeModifier swimModifier = new AttributeModifier(
                            SWIM_SPEED_MODIFIER_ID,
                            swimPenalty,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    );
                    swimSpeedAttribute.addPermanentModifier(swimModifier);
                }

                // Remove movement speed bonus if it exists
                if (movementSpeedAttribute != null) {
                    movementSpeedAttribute.removeModifier(MOVEMENT_SPEED_MODIFIER_ID);
                }

            } else {
                // Level 12: +1.5 of step height, +5% movement speed, no swim penalty
                if (stepHeightAttribute != null) {
                    stepHeightAttribute.removeModifier(STEP_HEIGHT_MODIFIER_ID);

                    AttributeModifier stepModifier = new AttributeModifier(
                            STEP_HEIGHT_MODIFIER_ID,
                            1.2,
                            AttributeModifier.Operation.ADD_VALUE
                    );
                    stepHeightAttribute.addPermanentModifier(stepModifier);
                }

                // Remove swim penalty
                if (swimSpeedAttribute != null) {
                    swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);
                }

                // Add movement speed bonus
                if (movementSpeedAttribute != null) {
                    movementSpeedAttribute.removeModifier(MOVEMENT_SPEED_MODIFIER_ID);

                    AttributeModifier speedModifier = new AttributeModifier(
                            MOVEMENT_SPEED_MODIFIER_ID,
                            0.055,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    );
                    movementSpeedAttribute.addPermanentModifier(speedModifier);
                }
            }

            APPLIED_LEVELS.put(playerId, level);
        }
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        // No continuous effects needed
    }

    @Override
    public void removeEffect(Player player) {
        var stepHeightAttribute = player.getAttribute(Attributes.STEP_HEIGHT);
        var swimSpeedAttribute = player.getAttribute(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED);
        var movementSpeedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (stepHeightAttribute != null) {
            stepHeightAttribute.removeModifier(STEP_HEIGHT_MODIFIER_ID);
        }
        if (swimSpeedAttribute != null) {
            swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);
        }
        if (movementSpeedAttribute != null) {
            movementSpeedAttribute.removeModifier(MOVEMENT_SPEED_MODIFIER_ID);
        }

        APPLIED_LEVELS.remove(player.getUUID());
    }
}