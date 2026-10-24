package com.nedraw.upgrading.disk;

public class BlueprintDisk extends UpgradeDisk {

    public BlueprintDisk() {
        super("blueprint", "Blueprint", DiskRarity.LEGENDARY);
    }

    public static float getCopyEfficiency(int level) {
        return switch (level) {
            case 9 -> 0.60f;
            case 10 -> 0.70f;
            case 11 -> 0.85f;
            case 12 -> 1.00f;
            default -> 0.60f;
        };
    }
}