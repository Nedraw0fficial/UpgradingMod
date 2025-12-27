package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;

public class SwiftFeetDisk extends UpgradeDisk {
    // Unique identifier for this disk's speed modifier
    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath("upgrading", "swift_feet_speed");

    // Cooldown tracking (we'll improve this with proper data storage later)
    private long lastDashTime = 0;
    private static final long DASH_COOLDOWN = 10000; // 10 seconds in milliseconds

    public SwiftFeetDisk() {
        super("swift_feet", "Swift Feet", DiskRarity.BASIC);

        // Set descriptions for each level
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
                .withDescription(12, "+60% movement speed and unlock ability to dash every 10s");
    }

    @Override
    public void applyEffect(Player player, int level) {
        // Calculate speed multiplier (5% per level)
        double speedMultiplier = (level * 5) / 100.0; // 0.05 for level 1, 0.60 for level 12

        // Get the movement speed attribute
        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (speedAttribute != null) {
            // Remove old modifier if it exists
            speedAttribute.removeModifier(SPEED_MODIFIER_ID);

            // Add new modifier with current level's speed
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

    private void handleDashAbility(Player player) {
        // Check if player just started sprinting while in the air
        long currentTime = System.currentTimeMillis();

        if (!player.onGround() && player.isSprinting() && (currentTime - lastDashTime) >= DASH_COOLDOWN) {
            // Get player's yaw (horizontal rotation only)
            float yaw = player.getYRot();
            double yawRadians = Math.toRadians(yaw);

            // Calculate horizontal direction based on yaw
            double dashPower = 1.5;
            double motionX = -Math.sin(yawRadians) * dashPower;
            double motionZ = Math.cos(yawRadians) * dashPower;

            // Apply dash velocity (horizontal + small upward boost)
            player.setDeltaMovement(
                    motionX,
                    0.3, // Small upward boost
                    motionZ
            );

            // Play sound effect (optional, we'll add this later)
            // player.playSound(SoundEvents.ENDER_DRAGON_FLAP, 0.5f, 2.0f);

            // Update cooldown
            lastDashTime = currentTime;

            // Notify player (optional)
            // player.displayClientMessage(Component.literal("DASH!"), true);
        }
    }
}