package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SwiftFeetDisk extends UpgradeDisk {
    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "swift_feet_speed");

    // Per-player cooldown tracking
    private static final Map<UUID, Long> DASH_COOLDOWNS = new HashMap<>();
    private static final long DASH_COOLDOWN = 10000; // 10 seconds

    // Track which level is currently applied to each player
    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    // Track previous vertical velocity for dash detection
    private static final Map<UUID, Double> LAST_Y_VELOCITY = new HashMap<>();

    public SwiftFeetDisk() {
        super("swift_feet", "Swift Feet", DiskRarity.BASIC);

        this.withDescription(1, "+3% movement speed")
                .withDescription(2, "+6% movement speed")
                .withDescription(3, "+9% movement speed")
                .withDescription(4, "+12% movement speed")
                .withDescription(5, "+15% movement speed")
                .withDescription(6, "+18% movement speed")
                .withDescription(7, "+21% movement speed")
                .withDescription(8, "+24% movement speed")
                .withDescription(9, "+27% movement speed")
                .withDescription(10, "+30% movement speed")
                .withDescription(11, "+33% movement speed")
                .withDescription(12, "+36% movement speed and unlock\nthe ability to dash");
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        // Only update attributes if level changed
        if (appliedLevel == null || appliedLevel != level) {
            // Calculate speed multiplier (3% per level)
            double speedMultiplier = (level * 3) / 100.0;

            var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

            if (speedAttribute != null) {
                // Remove old modifier if exists
                speedAttribute.removeModifier(SPEED_MODIFIER_ID);

                // Add new modifier
                AttributeModifier speedModifier = new AttributeModifier(
                        SPEED_MODIFIER_ID,
                        speedMultiplier,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );

                speedAttribute.addPermanentModifier(speedModifier);
            }

            // Mark this level as applied
            APPLIED_LEVELS.put(playerId, level);
        }

        // ONLY check for dash if level 12+ (to minimize performance impact)
        if (level >= 12) {
            handleDashDetection(player);
        }
    }

    @Override
    public void removeEffect(Player player) {
        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_ID);
        }

        // Clear tracking
        UUID playerId = player.getUUID();
        APPLIED_LEVELS.remove(playerId);
        LAST_Y_VELOCITY.remove(playerId);
    }

    private void handleDashDetection(Player player) {
        UUID playerId = player.getUUID();

        // Don't run on client side
        if (player.level().isClientSide) return;

        long currentTime = System.currentTimeMillis();
        long lastDash = DASH_COOLDOWNS.getOrDefault(playerId, 0L);

        // Check if player is in air and cooldown is ready
        if (!player.onGround() && (currentTime - lastDash) >= DASH_COOLDOWN) {
            double currentYVel = player.getDeltaMovement().y;
            Double lastYVel = LAST_Y_VELOCITY.get(playerId);

            // Detect jump: sudden upward velocity change
            if (lastYVel != null && currentYVel > 0.3 && lastYVel < 0.3) {
                performDash(player);
            }

            LAST_Y_VELOCITY.put(playerId, currentYVel);
        } else if (player.onGround()) {
            // Reset velocity tracking when on ground
            LAST_Y_VELOCITY.put(playerId, 0.0);
        }
    }

    private void performDash(Player player) {
        UUID playerId = player.getUUID();

        // Get player's look direction (horizontal only)
        float yaw = player.getYRot();
        double yawRadians = Math.toRadians(yaw);

        // Dash in the direction player is looking
        double dashPower = 2.5;
        double motionX = -Math.sin(yawRadians) * dashPower;
        double motionZ = Math.cos(yawRadians) * dashPower;

        // Apply velocity boost
        player.setDeltaMovement(motionX, 0.4, motionZ);
        player.hurtMarked = true;

        // Update cooldown
        DASH_COOLDOWNS.put(playerId, System.currentTimeMillis());

        // Play sound effect
        player.level().playSound(
                null,
                player.blockPosition(),
                net.minecraft.sounds.SoundEvents.ENDER_DRAGON_FLAP,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.7f,
                1.8f
        );
    }
}