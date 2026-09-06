package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

public class BrewmasterDisk extends UpgradeDisk {

    public BrewmasterDisk() {
        super("brewmaster", "Brewmaster", DiskRarity.EPIC);
    }

    public float getChance(int level, float efficiency) {
        float base = switch (level) {
            case 7  -> 0.12f;
            case 8  -> 0.16f;
            case 9  -> 0.20f;
            case 10 -> 0.24f;
            case 11 -> 0.31f;
            case 12 -> 0.40f;
            default -> 0.12f;
        };
        return Math.min(base * efficiency, 0.95f);
    }

    public int getMaxExtraEffects(int level) {
        return switch (level) {
            case 7, 8, 9 -> 1;
            case 10, 11  -> 2;
            case 12      -> 3;
            default      -> 1;
        };
    }

    public float getDurationMultiplier(int level, float efficiency) {
        if (level < 12) return 1.0f;
        return 1.0f + (0.26f * efficiency);
    }
}