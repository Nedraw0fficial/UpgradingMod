package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

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

    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    // Track Y position to detect step-ups
    private static final Map<UUID, Double> LAST_Y = new HashMap<>();

    public StepAssistDisk() {
        super("step_assist", "Step Assist", DiskRarity.RARE);
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        if (appliedLevel == null || appliedLevel != level) {
            var stepHeightAttribute = player.getAttribute(Attributes.STEP_HEIGHT);
            var swimSpeedAttribute = player.getAttribute(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED);
            var movementSpeedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

            if (level < 12) {
                if (stepHeightAttribute != null) {
                    stepHeightAttribute.removeModifier(STEP_HEIGHT_MODIFIER_ID);
                    double stepBonus = (level - 3) * 0.1;
                    stepHeightAttribute.addPermanentModifier(new AttributeModifier(
                            STEP_HEIGHT_MODIFIER_ID, stepBonus,
                            AttributeModifier.Operation.ADD_VALUE));
                }
                if (swimSpeedAttribute != null) {
                    swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);
                    double swimPenalty = -((12 - level) * 4) / 100.0;
                    swimSpeedAttribute.addPermanentModifier(new AttributeModifier(
                            SWIM_SPEED_MODIFIER_ID, swimPenalty,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
                if (movementSpeedAttribute != null) {
                    movementSpeedAttribute.removeModifier(MOVEMENT_SPEED_MODIFIER_ID);
                }
            } else {
                if (stepHeightAttribute != null) {
                    stepHeightAttribute.removeModifier(STEP_HEIGHT_MODIFIER_ID);
                    stepHeightAttribute.addPermanentModifier(new AttributeModifier(
                            STEP_HEIGHT_MODIFIER_ID, 1.2,
                            AttributeModifier.Operation.ADD_VALUE));
                }
                if (swimSpeedAttribute != null) {
                    swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);
                }
                if (movementSpeedAttribute != null) {
                    movementSpeedAttribute.removeModifier(MOVEMENT_SPEED_MODIFIER_ID);
                    movementSpeedAttribute.addPermanentModifier(new AttributeModifier(
                            MOVEMENT_SPEED_MODIFIER_ID, 0.055,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
            }

            APPLIED_LEVELS.put(playerId, level);
        }
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        if (player.level().isClientSide) return;

        UUID playerId = player.getUUID();
        double currentY = player.getY();
        double lastY = LAST_Y.getOrDefault(playerId, currentY);

        double yDelta = currentY - lastY;

        // A step-up of >= 1.0 block while on the ground (not jumping)
        // onGround means they were grounded last tick, and now they're higher
        // We check yDelta >= 0.9 (slightly under 1.0 to account for float precision)
        // and that the player isn't jumping (no vertical velocity from jump key)
        // yDelta >= 0.9 = stepped up ~1 full block
        // onGround = they've landed on the new surface
        // getDeltaMovement().y <= 0.1 = not currently jumping upward
        if (yDelta >= 0.9 && player.onGround() && player.getDeltaMovement().y <= 0.1) {
            if (player instanceof ServerPlayer sp) {
                ModAdvancementTriggers.STEP_UP_FULL_BLOCK(sp);
            }
        }

        LAST_Y.put(playerId, currentY);
    }

    @Override
    public void removeEffect(Player player) {
        var stepHeightAttribute = player.getAttribute(Attributes.STEP_HEIGHT);
        var swimSpeedAttribute = player.getAttribute(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED);
        var movementSpeedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (stepHeightAttribute != null) stepHeightAttribute.removeModifier(STEP_HEIGHT_MODIFIER_ID);
        if (swimSpeedAttribute != null) swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);
        if (movementSpeedAttribute != null) movementSpeedAttribute.removeModifier(MOVEMENT_SPEED_MODIFIER_ID);

        UUID playerId = player.getUUID();
        APPLIED_LEVELS.remove(playerId);
        LAST_Y.remove(playerId);
    }
}