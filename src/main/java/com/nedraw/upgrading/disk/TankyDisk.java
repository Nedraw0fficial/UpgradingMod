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
                        0.30, // +30% health
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
                        -0.21, // -21% speed
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
            case 2 -> 0.06f;   // +6%
            case 3 -> 0.07f;   // +7%
            case 4 -> 0.10f;   // +10%
            case 5 -> 0.12f;   // +12%
            case 6 -> 0.14f;   // +14%
            case 7 -> 0.16f;   // +16%
            case 8 -> 0.20f;   // +20%
            case 9 -> 0.22f;   // +22%
            case 10 -> 0.24f;  // +24%
            case 11 -> 0.26f;  // +26%
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