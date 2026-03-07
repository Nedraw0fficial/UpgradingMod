package com.nedraw.upgrading.disk;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class TankyDisk extends UpgradeDisk {

    private static final ResourceLocation HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "tanky_health");
    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "tanky_speed");

    public TankyDisk() {
        super("tanky", "Tanky", DiskRarity.BASIC);

        this.withDescription(1, "+5% max health, -35% movement speed")
                .withDescription(2, "+8% max health, -32% movement speed")
                .withDescription(3, "+11% max health, -29% movement speed")
                .withDescription(4, "+14% max health, -26% movement speed")
                .withDescription(5, "+17% max health, -24% movement speed")
                .withDescription(6, "+20% max health, -22% movement speed")
                .withDescription(7, "+23% max health, -21% movement speed")
                .withDescription(8, "+26% max health, -20% movement speed")
                .withDescription(9, "+29% max health, -19% movement speed")
                .withDescription(10, "+32% max health, -17% movement speed")
                .withDescription(11, "+36% max health, -15% movement speed")
                .withDescription(12, "+40% max health, -22% speed only when sprinting");
    }

    @Override
    public void applyEffect(Player player, int level) {
        if (level < 12) {
            // Levels 1-11: Permanent HP boost and speed reduction
            applyPermanentModifiers(player, level);
        }
        // Level 12 is handled in applyTickEffect
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        if (level >= 12) {
            // Level 12: Dynamic speed reduction only when sprinting
            applyLevel12Effect(player);
        }
    }

    @Override
    public void removeEffect(Player player) {
        // Remove all modifiers
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);
        }
        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_MODIFIER_ID);
        }

        // Heal player to new max health if needed
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private void applyPermanentModifiers(Player player, int level) {
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);

        float healthBoost = getHealthBoost(level);
        float speedReduction = getSpeedReduction(level);

        // Apply health boost
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);
            AttributeModifier healthMod = new AttributeModifier(
                    HEALTH_MODIFIER_ID,
                    healthBoost,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );
            healthAttr.addPermanentModifier(healthMod);
        }

        // Apply speed reduction
        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_MODIFIER_ID);
            AttributeModifier speedMod = new AttributeModifier(
                    SPEED_MODIFIER_ID,
                    -speedReduction,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );
            speedAttr.addPermanentModifier(speedMod);
        }
    }

    private void applyLevel12Effect(Player player) {
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);

        boolean isSprinting = player.isSprinting();

        // Always apply health boost
        if (healthAttr != null) {
            boolean hasHealthMod = healthAttr.hasModifier(HEALTH_MODIFIER_ID);
            if (!hasHealthMod) {
                AttributeModifier healthMod = new AttributeModifier(
                        HEALTH_MODIFIER_ID,
                        0.40, // +40% health
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                healthAttr.addPermanentModifier(healthMod);
            }
        }

        // Only apply speed reduction when sprinting
        if (speedAttr != null) {
            boolean hasSpeedMod = speedAttr.hasModifier(SPEED_MODIFIER_ID);

            if (isSprinting && !hasSpeedMod) {
                // Add speed reduction when starting to sprint
                AttributeModifier speedMod = new AttributeModifier(
                        SPEED_MODIFIER_ID,
                        -0.22, // -22% speed
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                speedAttr.addPermanentModifier(speedMod);
            } else if (!isSprinting && hasSpeedMod) {
                // Remove speed reduction when stopping sprint
                speedAttr.removeModifier(SPEED_MODIFIER_ID);
            }
        }
    }

    private float getHealthBoost(int level) {
        return switch (level) {
            case 1 -> 0.05f;   // +5%
            case 2 -> 0.08f;   // +8%
            case 3 -> 0.11f;   // +11%
            case 4 -> 0.14f;   // +14%
            case 5 -> 0.17f;   // +17%
            case 6 -> 0.20f;   // +20%
            case 7 -> 0.23f;   // +23%
            case 8 -> 0.26f;   // +26%
            case 9 -> 0.29f;   // +29%
            case 10 -> 0.32f;  // +32%
            case 11 -> 0.36f;  // +36%
            default -> 0.05f;
        };
    }

    private float getSpeedReduction(int level) {
        return switch (level) {
            case 1 -> 0.35f;   // -35%
            case 2 -> 0.32f;   // -32%
            case 3 -> 0.29f;   // -29%
            case 4 -> 0.26f;   // -26%
            case 5 -> 0.24f;   // -24%
            case 6 -> 0.22f;   // -22%
            case 7 -> 0.21f;   // -21%
            case 8 -> 0.20f;   // -20%
            case 9 -> 0.19f;   // -19%
            case 10 -> 0.17f;  // -17%
            case 11 -> 0.15f;  // -15%
            default -> 0.35f;
        };
    }
}