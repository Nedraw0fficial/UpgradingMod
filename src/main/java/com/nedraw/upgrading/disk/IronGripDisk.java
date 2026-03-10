package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

public class IronGripDisk extends UpgradeDisk {

    public IronGripDisk() {
        super("iron_grip", "Iron Grip", DiskRarity.RARE);

        this.withDescription(4, "Knockback reduced by 5%")
                .withDescription(5, "Knockback reduced by 7%")
                .withDescription(6, "Knockback reduced by 9%")
                .withDescription(7, "Knockback reduced by 12%")
                .withDescription(8, "Knockback reduced by 15%")
                .withDescription(9, "Knockback reduced by 19%")
                .withDescription(10, "Knockback reduced by 23%")
                .withDescription(11, "Knockback reduced by 30%")
                .withDescription(12, "Knockback reduced by 40% and can't\nbe knocked off edges while sneaking");
    }

    @Override
    public void applyEffect(Player player, int level) {
        // Knockback reduction handled in IronGripHandler
    }

    @Override
    public void removeEffect(Player player) {
        // Nothing to clean up
    }

    public float getKnockbackReduction(int level) {
        return switch (level) {
            case 4 -> 0.05f;
            case 5 -> 0.07f;
            case 6 -> 0.09f;
            case 7 -> 0.12f;
            case 8 -> 0.15f;
            case 9 -> 0.19f;
            case 10 -> 0.23f;
            case 11 -> 0.30f;
            case 12 -> 0.40f;
            default -> 0.05f;
        };
    }
}