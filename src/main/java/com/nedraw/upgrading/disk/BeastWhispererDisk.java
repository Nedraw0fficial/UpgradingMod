package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

import java.util.Random;

public class BeastWhispererDisk extends UpgradeDisk {

    private static final Random RANDOM = new Random();

    public BeastWhispererDisk() {
        super("beast_whisperer", "Beast Whisperer", DiskRarity.RARE);

        this.withDescription(4, "Animals breeding cooldown is 10% faster")
                .withDescription(5, "Animals breeding cooldown is 15% faster")
                .withDescription(6, "Animals breeding cooldown is 20% faster")
                .withDescription(7, "Animals breeding cooldown is 25% faster")
                .withDescription(8, "Animals breeding cooldown is 30% faster")
                .withDescription(9, "Animals breeding cooldown is 35% faster")
                .withDescription(10, "Animals breeding cooldown is 45% faster")
                .withDescription(11, "Animals breeding cooldown is 60% faster")
                .withDescription(12, "Animals breeding cooldown is 80% faster\nand 12% chance for twins (recursive)");
    }

    @Override
    public void applyEffect(Player player, int level) {
        // Breeding speed is handled in event listener
    }

    @Override
    public void removeEffect(Player player) {
        // Nothing to clean up
    }

    // Called from breeding event
    public int getReducedBreedingCooldown(int originalCooldown, int level) {
        float reduction = getBreedingSpeedBonus(level);
        return (int) (originalCooldown * (1.0f - reduction));
    }

    // Called from breeding event (level 12 bonus)
    public boolean shouldSpawnTwin(int level) {
        if (level >= 12) {
            return RANDOM.nextFloat() < 0.12f;
        }
        return false;
    }

    public float getBreedingSpeedBonus(int level) {
        return switch (level) {
            case 4 -> 0.10f;   // 10%
            case 5 -> 0.15f;   // 15%
            case 6 -> 0.20f;   // 20%
            case 7 -> 0.25f;   // 25%
            case 8 -> 0.30f;   // 30%
            case 9 -> 0.35f;   // 35%
            case 10 -> 0.45f;  // 45%
            case 11 -> 0.60f;  // 60%
            case 12 -> 0.80f;  // 80%
            default -> 0.10f;
        };
    }
}