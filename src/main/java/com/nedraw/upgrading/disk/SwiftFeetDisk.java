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

    // Per-player cooldown and state tracking
    private static final Map<UUID, Long> DASH_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Boolean> WAS_JUMPING = new HashMap<>();
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

        // Check if player is trying to jump by checking vertical velocity
        // When player presses jump while in air, there's a small upward velocity spike
        double verticalVelocity = player.getDeltaMovement().y;

        // Detect jump attempt: positive Y velocity while in air (trying to jump)
        boolean isTryingToJump = verticalVelocity > 0.0 && verticalVelocity < 0.42; // Jump gives ~0.42 velocity

        // Get previous jump state
        boolean wasTryingToJump = WAS_JUMPING.getOrDefault(playerId, false);

        // Detect jump key press (transition)
        boolean jumpKeyPressed = isTryingToJump && !wasTryingToJump;

        // Update tracking for next tick
        WAS_JUMPING.put(playerId, isTryingToJump);

        // Dash conditions:
        // 1. Player is in the air
        // 2. Player pressed jump key (detected via velocity spike)
        // 3. Cooldown is ready
        if (!player.onGround() && jumpKeyPressed && (currentTime - lastDash) >= DASH_COOLDOWN) {
            // Get player's look direction (horizontal only)
            float yaw = player.getYRot();
            double yawRadians = Math.toRadians(yaw);

            // Dash in the direction player is looking
            double dashPower = 1.0; // Strong forward boost
            double motionX = -Math.sin(yawRadians) * dashPower;
            double motionZ = Math.cos(yawRadians) * dashPower;

            // Apply strong velocity boost with slight upward component
            player.setDeltaMovement(motionX, 0.4, motionZ);

            // Mark velocity changed for proper sync
            player.hurtMarked = true;

            // Update cooldown
            DASH_COOLDOWNS.put(playerId, currentTime);

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
}