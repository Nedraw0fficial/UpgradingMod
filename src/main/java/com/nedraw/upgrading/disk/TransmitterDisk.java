package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

public class TransmitterDisk extends UpgradeDisk {

    public TransmitterDisk() {
        super("transmitter", "Transmitter", DiskRarity.LEGENDARY);
    }

    public double getRange(int level, float efficiency) {
        double base = switch (level) {
            case 9  -> 6.0;
            case 10 -> 7.0;
            case 11 -> 9.0;
            case 12 -> 11.0;
            default -> 6.0;
        };
        return base * efficiency;
    }

    public float getTransmitPercent(int level, float efficiency) {
        float base = switch (level) {
            case 9  -> 0.10f;
            case 10 -> 0.125f;
            case 11 -> 0.15f;
            case 12 -> 0.215f;
            default -> 0.10f;
        };
        return base * efficiency;
    }

    public float getDamageReduction(int level, float efficiency) {
        if (level < 12) return 0.0f;
        return 0.12f * efficiency;
    }
}