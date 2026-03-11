package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

public class MountainGoatDisk extends UpgradeDisk {

    public MountainGoatDisk() {
        super("mountain_goat", "Mountain Goat", DiskRarity.EPIC);

        this.withDescription(7, "Cling to walls for 1.0 second")
                .withDescription(8, "Cling to walls for 1.25 seconds")
                .withDescription(9, "Cling to walls for 1.5 seconds")
                .withDescription(10, "Cling to walls for 1.75 seconds")
                .withDescription(11, "Cling to walls for 2.0 seconds")
                .withDescription(12, "Cling to walls for 2.5 seconds + wall jump");
    }

    @Override
    public void applyEffect(Player player, int level) {
        // ALL LOGIC IS IN MountainGoatHandler!
        // This method intentionally left empty
    }

    @Override
    public void removeEffect(Player player) {
        // Cleanup happens in MountainGoatHandler
    }
}