package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.Math.round;

public class SeaFishDisk extends UpgradeDisk {
    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "sea_fish_speed");
    private static final ResourceLocation SWIM_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "sea_fish_swim_speed");

    // Track which level is currently applied to each player
    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    // Track if bonus air has been used (reset when player surfaces)
    private static final Map<UUID, Boolean> BONUS_AIR_USED = new HashMap<>();

    public SeaFishDisk() {
        super("sea_fish", "Sea Fish", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        // Only update attributes if level changed
        if (appliedLevel == null || appliedLevel != level) {
            var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
            var swimSpeedAttribute = player.getAttribute(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED);

            if (level < 12) {
                // Levels 1-11: Penalty on land
                if (speedAttribute != null) {
                    speedAttribute.removeModifier(SPEED_MODIFIER_ID);

                    double speedPenalty = - (22 - (level * 2)) / 100.0; // Starts at -20% and +2% per level
                    AttributeModifier speedModifier = new AttributeModifier(
                            SPEED_MODIFIER_ID,
                            speedPenalty,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    );
                    speedAttribute.addPermanentModifier(speedModifier);
                }

                // Remove swim speed bonus if it exists
                if (swimSpeedAttribute != null) {
                    swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);
                }
            } else {
                // Level 12: Remove penalty, add swim speed
                if (speedAttribute != null) {
                    speedAttribute.removeModifier(SPEED_MODIFIER_ID);
                }

                // Add swim speed using NeoForge attribute
                if (swimSpeedAttribute != null) {
                    swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);

                    double swimBoost = 0.12; // 12% faster
                    AttributeModifier swimModifier = new AttributeModifier(
                            SWIM_SPEED_MODIFIER_ID,
                            swimBoost,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    );
                    swimSpeedAttribute.addPermanentModifier(swimModifier);
                }
            }

            APPLIED_LEVELS.put(playerId, level);
        }
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        // Server-side only
        if (player.level().isClientSide) return;

        UUID playerId = player.getUUID();

        // All levels use the same bonus air system
        if (player.isUnderWater()) {
            // Check if player is about to drown (air <= 0) and hasn't used bonus yet
            boolean bonusUsed = BONUS_AIR_USED.getOrDefault(playerId, false);
            if (player.getAirSupply() <= 0 && !bonusUsed) {
                // Calculate bonus air based on level
                int airBonus;
                if (level < 12) {
                    airBonus = Math.toIntExact(2 + round(level * level * 0.1)); // In seconds
                } else {
                    airBonus = 20; // Level 12: 20 seconds
                }
                int airBonusTicks = airBonus * 20; // Convert to ticks

                // Add bonus air (CAN exceed max!)
                player.setAirSupply(airBonusTicks);

                // Mark bonus as used
                BONUS_AIR_USED.put(playerId, true);
            }
        } else {
            // Player NOT underwater (surfaced) - reset bonus availability
            BONUS_AIR_USED.put(playerId, false);
        }
    }

    @Override
    public void removeEffect(Player player) {
        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var swimSpeedAttribute = player.getAttribute(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED);

        if (speedAttribute != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_ID);
        }
        if (swimSpeedAttribute != null) {
            swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);
        }

        UUID playerId = player.getUUID();
        APPLIED_LEVELS.remove(playerId);
        BONUS_AIR_USED.remove(playerId);
    }
}