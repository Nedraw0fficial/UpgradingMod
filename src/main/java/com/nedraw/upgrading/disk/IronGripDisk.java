package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

public class IronGripDisk extends UpgradeDisk {

    public IronGripDisk() {
        super("iron_grip", "Iron Grip", DiskRarity.RARE);
    }

    @Override
    public void applyEffect(Player player, int level) {}

    @Override
    public void removeEffect(Player player) {}

    public float getKnockbackReduction(int level, float efficiency) {
        float base = switch (level) {
            case 4 -> 0.05f; case 5 -> 0.07f; case 6 -> 0.09f;
            case 7 -> 0.12f; case 8 -> 0.15f; case 9 -> 0.19f;
            case 10 -> 0.23f; case 11 -> 0.30f; case 12 -> 0.40f;
            default -> 0.05f;
        };
        return Math.min(base * efficiency, 0.95f); // cap at 95%
    }
}
