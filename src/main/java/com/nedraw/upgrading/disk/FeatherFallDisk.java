package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

public class FeatherFallDisk extends UpgradeDisk {

    public FeatherFallDisk() {
        super("feather_fall", "Feather Fall", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level) {}

    @Override
    public void removeEffect(Player player) {}

    public float reduceFallDamage(float originalDamage, int level, float efficiency) {
        if (level >= 12 && originalDamage <= 2.0f * efficiency) return 0.0f;
        float reduction = getReductionPercent(level) * efficiency;
        return originalDamage * (1.0f - Math.min(reduction, 0.95f));
    }

    private float getReductionPercent(int level) {
        return switch (level) {
            case 1 -> 0.05f; case 2 -> 0.08f; case 3 -> 0.11f;
            case 4 -> 0.14f; case 5 -> 0.17f; case 6 -> 0.20f;
            case 7 -> 0.23f; case 8 -> 0.26f; case 9 -> 0.29f;
            case 10 -> 0.32f; case 11 -> 0.36f; case 12 -> 0.40f;
            default -> 0.05f;
        };
    }
}
