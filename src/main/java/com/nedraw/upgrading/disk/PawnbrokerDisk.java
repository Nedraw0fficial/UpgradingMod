package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

public class PawnbrokerDisk extends UpgradeDisk {

    public PawnbrokerDisk() {
        super("pawnbroker", "Pawnbroker", DiskRarity.RARE);
    }

    @Override
    public void applyEffect(Player player, int level) {
        // Passive effect - handled in event handler
    }

    @Override
    public void removeEffect(Player player) {
        // No cleanup needed
    }

    public double getDiscount(int level) {
        return switch (level) {
            case 4 -> 0.10;
            case 5 -> 0.12;
            case 6 -> 0.14;
            case 7 -> 0.16;
            case 8 -> 0.18;
            case 9 -> 0.20;
            case 10 -> 0.25;
            case 11 -> 0.30;
            case 12 -> 0.33;
            default -> 0.0;
        };
    }

    public boolean canRefundEmeralds(int level) {
        return level >= 12;
    }

    public double getRefundChance() {
        return 0.02; // 2%
    }
}