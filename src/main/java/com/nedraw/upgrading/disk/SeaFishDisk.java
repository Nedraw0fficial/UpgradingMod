package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.server.level.ServerPlayer;
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

    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();
    private static final Map<UUID, Boolean> BONUS_AIR_USED = new HashMap<>();

    // Track consecutive ticks spent underwater
    private static final Map<UUID, Integer> UNDERWATER_TICKS = new HashMap<>();

    public SeaFishDisk() {
        super("sea_fish", "Sea Fish", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        if (appliedLevel == null || appliedLevel != level) {
            var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
            var swimSpeedAttribute = player.getAttribute(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED);

            if (level < 12) {
                if (speedAttribute != null) {
                    speedAttribute.removeModifier(SPEED_MODIFIER_ID);
                    double speedPenalty = -(22 - (level * 2)) / 100.0;
                    speedAttribute.addPermanentModifier(new AttributeModifier(
                            SPEED_MODIFIER_ID, speedPenalty,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
                if (swimSpeedAttribute != null) {
                    swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);
                }
            } else {
                if (speedAttribute != null) speedAttribute.removeModifier(SPEED_MODIFIER_ID);
                if (swimSpeedAttribute != null) {
                    swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);
                    swimSpeedAttribute.addPermanentModifier(new AttributeModifier(
                            SWIM_SPEED_MODIFIER_ID, 0.12,
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

        if (player.isUnderWater()) {
            // Increment underwater tick counter
            int ticks = UNDERWATER_TICKS.getOrDefault(playerId, 0) + 1;
            UNDERWATER_TICKS.put(playerId, ticks);

            // Fire advancement at exactly 600 ticks (30 seconds)
            if (ticks == 600 && player instanceof ServerPlayer sp) {
                ModAdvancementTriggers.UNDERWATER_30S(sp);
            }

            // Bonus air logic (unchanged)
            boolean bonusUsed = BONUS_AIR_USED.getOrDefault(playerId, false);
            if (player.getAirSupply() <= 0 && !bonusUsed) {
                int airBonus = level < 12
                        ? Math.toIntExact(2 + round(level * level * 0.1))
                        : 20;
                player.setAirSupply(airBonus * 20);
                BONUS_AIR_USED.put(playerId, true);
            }
        } else {
            // Surfaced - reset both counters
            UNDERWATER_TICKS.put(playerId, 0);
            BONUS_AIR_USED.put(playerId, false);
        }
    }

    @Override
    public void removeEffect(Player player) {
        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var swimSpeedAttribute = player.getAttribute(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED);

        if (speedAttribute != null) speedAttribute.removeModifier(SPEED_MODIFIER_ID);
        if (swimSpeedAttribute != null) swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);

        UUID playerId = player.getUUID();
        APPLIED_LEVELS.remove(playerId);
        BONUS_AIR_USED.remove(playerId);
        UNDERWATER_TICKS.remove(playerId);
    }
}