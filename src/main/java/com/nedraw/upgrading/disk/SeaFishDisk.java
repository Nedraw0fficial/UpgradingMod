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

public class SeaFishDisk extends UpgradeDisk {
    private static final ResourceLocation SWIM_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "sea_fish_swim_speed");
    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();
    private static final Map<UUID, Boolean> BONUS_AIR_USED = new HashMap<>();
    private static final Map<UUID, Integer> UNDERWATER_TICKS = new HashMap<>();

    public SeaFishDisk() {
        super("sea_fish", "Sea Fish", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level, int slot, float efficiency) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        if (appliedLevel == null || appliedLevel != level) {
            var swimSpeedAttribute = player.getAttribute(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED);
            if (swimSpeedAttribute != null) {
                swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);
                if (level >= 12) {
                    swimSpeedAttribute.addPermanentModifier(new AttributeModifier(
                            SWIM_SPEED_MODIFIER_ID, 0.12 * efficiency,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
            }
            APPLIED_LEVELS.put(playerId, level);
        }
    }

    @Override
    public void applyTickEffect(Player player, int level, int slot, float efficiency) {
        if (player.level().isClientSide) return;
        UUID playerId = player.getUUID();

        if (player.isUnderWater()) {
            int ticks = UNDERWATER_TICKS.getOrDefault(playerId, 0) + 1;
            UNDERWATER_TICKS.put(playerId, ticks);
            if (ticks == 500 && player instanceof ServerPlayer sp) {
                ModAdvancementTriggers.UNDERWATER_30S(sp);
            }

            boolean bonusUsed = BONUS_AIR_USED.getOrDefault(playerId, false);
            if (player.getAirSupply() <= 0 && !bonusUsed) {
                int bonusSeconds = getAirBonus(level);
                player.setAirSupply((int)(bonusSeconds * 20 * efficiency));
                BONUS_AIR_USED.put(playerId, true);
            }
        } else {
            UNDERWATER_TICKS.put(playerId, 0);
            BONUS_AIR_USED.put(playerId, false);
        }
    }

    @Override
    public void removeEffect(Player player) {
        var swimSpeedAttribute = player.getAttribute(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED);
        if (swimSpeedAttribute != null) swimSpeedAttribute.removeModifier(SWIM_SPEED_MODIFIER_ID);
        UUID playerId = player.getUUID();
        APPLIED_LEVELS.remove(playerId);
        BONUS_AIR_USED.remove(playerId);
        UNDERWATER_TICKS.remove(playerId);
    }

    private int getAirBonus(int level) {
        return switch (level) {
            case 1 -> 1; case 2 -> 2; case 3 -> 3; case 4 -> 4;
            case 5 -> 5; case 6 -> 6; case 7 -> 7; case 8 -> 8;
            case 9 -> 9; case 10 -> 10; case 11 -> 12; case 12 -> 15;
            default -> 1;
        };
    }
}
