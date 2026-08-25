package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

public class TreasureSenseDisk extends UpgradeDisk {

    public TreasureSenseDisk() {
        super("treasure_sense", "Treasure Sense", DiskRarity.RARE);
    }

    @Override
    public void applyEffect(Player player, int level) {}

    @Override
    public void removeEffect(Player player) {}

    public float getDuplicationChance(int level, float efficiency) {
        float base = switch (level) {
            case 4  -> 0.08f;  case 5  -> 0.116f; case 6  -> 0.152f;
            case 7  -> 0.188f; case 8  -> 0.224f; case 9  -> 0.26f;
            case 10 -> 0.296f; case 11 -> 0.332f; case 12 -> 0.348f;
            default -> 0.08f;
        };
        return Math.min(base * efficiency, 0.95f);
    }

    // Backwards compat
    public float getDuplicationChance(int level) {
        return getDuplicationChance(level, 1.0f);
    }

    public int getDuplicationCount(int level) {
        return switch (level) {
            case 4, 5 -> 1; case 6, 7, 8 -> 2;
            case 9, 10 -> 3; case 11, 12 -> 4;
            default -> 1;
        };
    }

    public boolean hasGlowingChests(int level) { return level >= 12; }
}
