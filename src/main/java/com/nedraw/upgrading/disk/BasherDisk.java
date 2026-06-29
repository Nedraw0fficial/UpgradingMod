package com.nedraw.upgrading.disk;

public class BasherDisk extends UpgradeDisk {

    public BasherDisk() {
        super("basher", "Basher", DiskRarity.EPIC);
        // No withDescription() calls - translations handle it!
    }
}