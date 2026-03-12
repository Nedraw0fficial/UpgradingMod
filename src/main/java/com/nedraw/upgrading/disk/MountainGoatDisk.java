package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

public class MountainGoatDisk extends UpgradeDisk {

    public MountainGoatDisk() {
        super("mountain_goat", "Mountain Goat", DiskRarity.EPIC);
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