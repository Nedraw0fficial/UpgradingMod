package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

public class TreasureSenseDisk extends UpgradeDisk {

    public TreasureSenseDisk() {
        super("treasure_sense", "Treasure Sense", DiskRarity.RARE);

        this.withDescription(4, "Chests have 8% chance to duplicate 1 item")
                .withDescription(5, "Chests have 11.6% chance to duplicate 1 item")
                .withDescription(6, "Chests have 15.2% chance to duplicate 2 items")
                .withDescription(7, "Chests have 18.8% chance to duplicate 2 items")
                .withDescription(8, "Chests have 22.4% chance to duplicate 2 items")
                .withDescription(9, "Chests have 26% chance to duplicate 3 items")
                .withDescription(10, "Chests have 29.6% chance to duplicate 3 items")
                .withDescription(11, "Chests have 33.2% chance to duplicate 4 items")
                .withDescription(12, "Chests have 34.8% chance to duplicate 4 items\nand nearby chests glow through walls (8 blocks)");
    }

    @Override
    public void applyEffect(Player player, int level) {
        // Chest duplication handled in TreasureSenseHandler
        // Glowing handled in client rendering
    }

    @Override
    public void removeEffect(Player player) {
        // Nothing to clean up
    }

    public float getDuplicationChance(int level) {
        return switch (level) {
            case 4 -> 0.08f;    // 8%
            case 5 -> 0.116f;   // 11.6%
            case 6 -> 0.152f;   // 15.2%
            case 7 -> 0.188f;   // 18.8%
            case 8 -> 0.224f;   // 22.4%
            case 9 -> 0.26f;    // 26%
            case 10 -> 0.296f;  // 29.6%
            case 11 -> 0.332f;  // 33.2%
            case 12 -> 0.348f;  // 34.8%
            default -> 0.08f;
        };
    }

    public int getDuplicationCount(int level) {
        return switch (level) {
            case 4, 5 -> 1;
            case 6, 7, 8 -> 2;
            case 9, 10 -> 3;
            case 11, 12 -> 4;
            default -> 1;
        };
    }

    public boolean hasGlowingChests(int level) {
        return level >= 12;
    }
}