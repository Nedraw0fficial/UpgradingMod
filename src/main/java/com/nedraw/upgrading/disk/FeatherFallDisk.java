package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

public class FeatherFallDisk extends UpgradeDisk {

    public FeatherFallDisk() {
        super("feather_fall", "Feather Fall", DiskRarity.BASIC);
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