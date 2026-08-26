package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

import java.util.Random;

public class BeastWhispererDisk extends UpgradeDisk {

    private static final Random RANDOM = new Random();

    public BeastWhispererDisk() {
        super("beast_whisperer", "Beast Whisperer", DiskRarity.RARE);
    }

    @Override
    public void applyEffect(Player player, int level) {}

    @Override
    public void removeEffect(Player player) {}

    public int getReducedBreedingCooldown(int originalCooldown, int level, float efficiency) {
        float reduction = Math.min(getBreedingSpeedBonus(level) * efficiency, 0.95f);
        return (int)(originalCooldown * (1.0f - reduction));
    }

    // Backwards compat
    public int getReducedBreedingCooldown(int originalCooldown, int level) {
        return getReducedBreedingCooldown(originalCooldown, level, 1.0f);
    }

    public boolean shouldSpawnTwin(int level, float efficiency) {
        if (level >= 12) {
            return RANDOM.nextFloat() < Math.min(0.12f * efficiency, 0.50f);
        }
        return false;
    }

    public boolean shouldSpawnTwin(int level) {
        return shouldSpawnTwin(level, 1.0f);
    }

    public float getBreedingSpeedBonus(int level) {
        return switch (level) {
            case 4  -> 0.10f; case 5  -> 0.15f; case 6  -> 0.20f;
            case 7  -> 0.25f; case 8  -> 0.30f; case 9  -> 0.35f;
            case 10 -> 0.45f; case 11 -> 0.60f; case 12 -> 0.80f;
            default -> 0.10f;
        };
    }
}
