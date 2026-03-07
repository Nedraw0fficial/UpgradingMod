package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

public class FeatherFallDisk extends UpgradeDisk {

    public FeatherFallDisk() {
        super("feather_fall", "Feather Fall", DiskRarity.BASIC);

        this.withDescription(1, "Reduce fall damage by 5%")
                .withDescription(2, "Reduce fall damage by 8%")
                .withDescription(3, "Reduce fall damage by 11%")
                .withDescription(4, "Reduce fall damage by 14%")
                .withDescription(5, "Reduce fall damage by 17%")
                .withDescription(6, "Reduce fall damage by 20%")
                .withDescription(7, "Reduce fall damage by 23%")
                .withDescription(8, "Reduce fall damage by 26%")
                .withDescription(9, "Reduce fall damage by 29%")
                .withDescription(10, "Reduce fall damage by 32%")
                .withDescription(11, "Reduce fall damage by 36%")
                .withDescription(12, "Reduce fall damage by 40% and negate\n2.0 damage falls");
    }

    @Override
    public void applyEffect(Player player, int level) {
        // Fall damage is handled in event listener
    }

    @Override
    public void removeEffect(Player player) {
        // Nothing to clean up
    }

    // Called from damage event handler
    public float reduceFallDamage(float originalDamage, int level) {
        // Level 12 bonus: Negate small falls (under 5 blocks ~ 2-3 damage)
        if (level >= 12 && originalDamage <= 2.0f) {
            return 0.0f;
        }

        float reduction = getReductionPercent(level);
        return originalDamage * (1.0f - reduction);
    }

    private float getReductionPercent(int level) {
        return switch (level) {
            case 1 -> 0.05f;   // 5%
            case 2 -> 0.08f;   // 8%
            case 3 -> 0.11f;   // 11%
            case 4 -> 0.14f;   // 14%
            case 5 -> 0.17f;   // 17%
            case 6 -> 0.20f;   // 20%
            case 7 -> 0.23f;   // 23%
            case 8 -> 0.26f;   // 26%
            case 9 -> 0.29f;   // 29%
            case 10 -> 0.32f;  // 32%
            case 11 -> 0.36f;  // 36%
            case 12 -> 0.40f;  // 40%
            default -> 0.05f;
        };
    }
}