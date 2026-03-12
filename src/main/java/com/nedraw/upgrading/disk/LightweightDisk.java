package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class LightweightDisk extends UpgradeDisk {

    private static final Map<UUID, Integer> LAST_FOOD_LEVEL = new HashMap<>();
    private static final Random RANDOM = new Random();

    public LightweightDisk() {
        super("lightweight", "Lightweight", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level) {
        // Initialize tracking
        LAST_FOOD_LEVEL.put(player.getUUID(), player.getFoodData().getFoodLevel());
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        // Server-side only
        if (player.level().isClientSide) return;

        UUID playerId = player.getUUID();
        FoodData foodData = player.getFoodData();
        int currentFood = foodData.getFoodLevel();
        int lastFood = LAST_FOOD_LEVEL.getOrDefault(playerId, currentFood);

        // Check if hunger decreased
        if (currentFood < lastFood) {
            int lost = lastFood - currentFood;

            // Roll chance to prevent hunger loss
            float preventChance = getPreventChance(level);

            if (RANDOM.nextFloat() < preventChance) {
                // Success! Restore the lost hunger
                foodData.setFoodLevel(lastFood);
            }
        }

        // Update tracking with CURRENT food level (after potential restoration)
        LAST_FOOD_LEVEL.put(playerId, foodData.getFoodLevel());
    }

    @Override
    public void removeEffect(Player player) {
        LAST_FOOD_LEVEL.remove(player.getUUID());
    }

    private float getPreventChance(int level) {
        return switch (level) {
            case 1 -> 0.05f;   // 5%
            case 2 -> 0.10f;   // 10%
            case 3 -> 0.15f;   // 15%
            case 4 -> 0.20f;   // 20%
            case 5 -> 0.25f;   // 25%
            case 6 -> 0.30f;   // 30%
            case 7 -> 0.35f;   // 35%
            case 8 -> 0.40f;   // 40%
            case 9 -> 0.45f;   // 45%
            case 10 -> 0.50f;  // 50%
            case 11 -> 0.55f;  // 55%
            case 12 -> 0.60f;  // 60%
            default -> 0.05f;
        };
    }

    // Called from food consumption event
    public float getSaturationBonus(int level) {
        if (level >= 12) {
            return 0.15f; // 15% bonus saturation
        }
        return 0.0f;
    }
}