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
    private static final ResourceLocation WATER_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "sea_fish_water_speed");

    // Track which level is currently applied to each player
    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

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
                .withDescription(12, "Infinite water breathing but\n+8% swim speed");
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        // Only update if level changed
        if (appliedLevel == null || appliedLevel != level) {
            var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
            var waterSpeedAttribute = player.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY);

            if (level < 12) {
                // Levels 1-11: Penalty on land, water breathing
                if (speedAttribute != null) {
                    speedAttribute.removeModifier(SPEED_MODIFIER_ID);

                    double speedPenalty = 22 -(level * 2) / 100.0; // Starts at -20% and +2% per level
                    AttributeModifier speedModifier = new AttributeModifier(
                            SPEED_MODIFIER_ID,
                            speedPenalty,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    );
                    speedAttribute.addPermanentModifier(speedModifier);
                }

                // Water breathing based on level
                int airBonus = Math.toIntExact(2 + round(level * level * 0.1)); // (represented as ticks * 20)
                player.setAirSupply(Math.min(player.getAirSupply() + airBonus, player.getMaxAirSupply()));

            } else {
                // Level 12: Remove penalty, add water breathing and swim speed
                if (speedAttribute != null) {
                    speedAttribute.removeModifier(SPEED_MODIFIER_ID);
                }

                // Give water breathing effect
                player.addEffect(new MobEffectInstance(
                        MobEffects.WATER_BREATHING,
                        100, // 5 seconds duration, will be refreshed
                        0,
                        false,
                        false,
                        false
                ));

                // Add swim speed
                if (waterSpeedAttribute != null) {
                    waterSpeedAttribute.removeModifier(WATER_SPEED_MODIFIER_ID);

                    double swimBoost = 0.08; // 8% faster
                    AttributeModifier waterModifier = new AttributeModifier(
                            WATER_SPEED_MODIFIER_ID,
                            swimBoost,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    );
                    waterSpeedAttribute.addPermanentModifier(waterModifier);
                }
            }

            APPLIED_LEVELS.put(playerId, level);
        }

        // Level 12: Continuously refresh water breathing
        if (level >= 12) {
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
        var waterSpeedAttribute = player.getAttribute(Attributes.WATER_MOVEMENT_EFFICIENCY);

        if (speedAttribute != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_ID);
        }
        if (waterSpeedAttribute != null) {
            waterSpeedAttribute.removeModifier(WATER_SPEED_MODIFIER_ID);
        }

        // Remove water breathing effect
        player.removeEffect(MobEffects.WATER_BREATHING);

        APPLIED_LEVELS.remove(player.getUUID());
    }
}