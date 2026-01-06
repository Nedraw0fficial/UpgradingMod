package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoapyHandsDisk extends UpgradeDisk {

    // Track which level is currently applied to each player
    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    public SoapyHandsDisk() {
        super("soapy_hands", "Soapy Hands", DiskRarity.EPIC);

        this.withDescription(7, "2% chance to make enemies\ndrop their held item")
                .withDescription(8, "4% chance to make enemies\ndrop their held item")
                .withDescription(9, "6% chance to make enemies\ndrop their held item")
                .withDescription(10, "8% chance to make enemies\ndrop their held item")
                .withDescription(11, "10% chance to make enemies\ndrop their held item")
                .withDescription(12, "15% main hand, 8% for EVERY\nother inventory slot!");
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        // Update tracking
        if (appliedLevel == null || appliedLevel != level) {
            APPLIED_LEVELS.put(playerId, level);
        }

        // Effect is handled in attack event
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        // No continuous effects needed
    }

    @Override
    public void removeEffect(Player player) {
        APPLIED_LEVELS.remove(player.getUUID());
    }

    // Calculate drop chance based on level
    public float getDropChance(int level) {
        if (level < 12) {
            // Levels 7-11: 2% per level above start
            return ((level - 6) * 2) / 100.0f;
        } else {
            // Level 12: 15% for main hand, 8% for others
            return 0.15f; // This is used for main hand only
        }
    }

    // Check if should drop armor (level 12 only)
    public boolean canDropArmor(int level) {
        return level >= 12;
    }

    public int getAppliedLevel(UUID playerId) {
        return APPLIED_LEVELS.getOrDefault(playerId, 0);
    }
}