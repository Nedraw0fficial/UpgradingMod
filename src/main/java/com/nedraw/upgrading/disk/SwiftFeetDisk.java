package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SwiftFeetDisk extends UpgradeDisk {
    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "swift_feet_speed");

    // Per-player cooldown tracking
    private static final Map<UUID, Long> DASH_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Boolean> WAS_SPRINTING = new HashMap<>();
    private static final long DASH_COOLDOWN = 10000; // 10 seconds

    public SwiftFeetDisk() {
        super("swift_feet", "Swift Feet", DiskRarity.BASIC);

        this.withDescription(1, "+5% movement speed")
                .withDescription(2, "+10% movement speed")
                .withDescription(3, "+15% movement speed")
                .withDescription(4, "+20% movement speed")
                .withDescription(5, "+25% movement speed")
                .withDescription(6, "+30% movement speed")
                .withDescription(7, "+35% movement speed")
                .withDescription(8, "+40% movement speed")
                .withDescription(9, "+45% movement speed")
                .withDescription(10, "+50% movement speed")
                .withDescription(11, "+55% movement speed")
                .withDescription(12, "+60% movement speed and unlock dash ability (Sprint in air)");
    }

    @Override
    public void applyEffect(Player player, int level) {
        // Calculate speed multiplier (5% per level)
        double speedMultiplier = (level * 5) / 100.0;

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

        // Level 12 bonus: Dash ability
        if (level >= 12) {
            handleDashAbility(player);
        }
    }

    @Override
    public void removeEffect(Player player) {
        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_ID);
        }
    }

    private void handleDashAbility(Player player) {
        UUID playerId = player.getUUID();
        long currentTime = System.currentTimeMillis();
        long lastDash = DASH_COOLDOWNS.getOrDefault(playerId, 0L);

        boolean isSprinting = player.isSprinting();
        boolean wasSprinting = WAS_SPRINTING.getOrDefault(playerId, false);

        // Detect sprint key press (transition from not sprinting to sprinting)
        boolean justStartedSprinting = isSprinting && !wasSprinting;

        // Update sprint state
        WAS_SPRINTING.put(playerId, isSprinting);

        // Check if player just started sprinting while in air and cooldown ready
        if (!player.onGround() && justStartedSprinting && (currentTime - lastDash) >= DASH_COOLDOWN) {
            // Get horizontal direction only
            float yaw = player.getYRot();
            double yawRadians = Math.toRadians(yaw);

            double dashPower = 1.7;
            double motionX = -Math.sin(yawRadians) * dashPower;
            double motionZ = Math.cos(yawRadians) * dashPower;

            // Apply dash with upward boost
            player.setDeltaMovement(motionX, 0.8, motionZ);

            // Update cooldown
            DASH_COOLDOWNS.put(playerId, currentTime);

            // Play sound effect
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    net.minecraft.sounds.SoundEvents.ENDER_DRAGON_FLAP,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.5f,
                    2.0f
            );
        }
    }
}