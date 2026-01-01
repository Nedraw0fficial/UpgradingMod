package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

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

        this.withDescription(1, "+2s of air underwater but\n-20% movement speed on land")
                .withDescription(2, "+2s of air underwater but\n-18% movement speed on land")
                .withDescription(3, "+3s of air underwater but\n-16% movement speed on land")
                .withDescription(4, "+4s of air underwater but\n-14% movement speed on land")
                .withDescription(5, "+5s of air underwater but\n-12% movement speed on land")
                .withDescription(6, "+6s of air underwater but\n-10% movement speed on land")
                .withDescription(7, "+7s of air underwater but\n-8% movement speed on land")
                .withDescription(8, "+8s of air underwater but\n-6% movement speed on land")
                .withDescription(9, "+10s of air underwater but\n-4% movement speed on land")
                .withDescription(10, "+12s of air underwater but\n-2% movement speed on land")
                .withDescription(11, "+14s of air underwater")
                .withDescription(12, "Infinite water breathing\n+8% swim speed");
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
            } else {
                // Level 12: Remove penalty, add swim speed
                if (speedAttribute != null) {
                    speedAttribute.removeModifier(SPEED_MODIFIER_ID);
                }

                // Add swim speed using NeoForge attribute
                if (swimSpeedAttribute != null) {
                    swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);

                    double swimBoost = 0.08; // 8% faster
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

        if (level < 12) {
            // Check if player is underwater
            if (player.isUnderWater()) {
                // Check if player has full air (just went underwater or surfaced)
                if (player.getAirSupply() >= player.getMaxAirSupply()) {
                    // Reset bonus air availability
                    BONUS_AIR_USED.put(playerId, false);
                }

                // Check if player is about to drown (air <= 0) and hasn't used bonus yet
                boolean bonusUsed = BONUS_AIR_USED.getOrDefault(playerId, false);
                if (player.getAirSupply() <= 0 && !bonusUsed) {
                    // Calculate bonus air based on level
                    int airBonus = Math.toIntExact(2 + round(level * level * 0.1)); // In seconds
                    int airBonusTicks = airBonus * 20; // Convert to ticks

                    // Add bonus air
                    player.setAirSupply(airBonusTicks);

                    // Mark bonus as used
                    BONUS_AIR_USED.put(playerId, true);
                }
            } else {
                // Player not underwater - reset bonus availability
                BONUS_AIR_USED.put(playerId, false);
            }
        } else {
            // Level 12: Infinite water breathing effect
            if (!player.hasEffect(MobEffects.WATER_BREATHING)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.WATER_BREATHING,
                        100,
                        0,
                        false,
                        false,
                        false
                ));
            }
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

        // Remove water breathing effect
        player.removeEffect(MobEffects.WATER_BREATHING);

        UUID playerId = player.getUUID();
        APPLIED_LEVELS.remove(playerId);
        BONUS_AIR_USED.remove(playerId);
    }
}