package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

public class UndertakerDisk extends UpgradeDisk {

    public UndertakerDisk() {
        super("undertaker", "Undertaker", DiskRarity.LEGENDARY);
    }

    public int getItemsToKeep(int level) {
        return switch (level) {
            case 9  -> 1;
            case 10 -> 2;
            case 11 -> 3;
            case 12 -> 5;
            default -> 0;
        };
    }

    public boolean keepsEquippedDisk(int level) {
        return level >= 12;
    }
}