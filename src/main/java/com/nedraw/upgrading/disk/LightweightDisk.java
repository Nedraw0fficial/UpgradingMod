package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import java.util.*;

public class LightweightDisk extends UpgradeDisk {

    private static final Map<UUID, Integer> LAST_FOOD_LEVEL = new HashMap<>();
    private static final Random RANDOM = new Random();
    private static final Map<UUID, List<Long>> PREVENTION_TIMESTAMPS = new HashMap<>();

    public LightweightDisk() {
        super("lightweight", "Lightweight", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level) {
        LAST_FOOD_LEVEL.put(player.getUUID(), player.getFoodData().getFoodLevel());
    }

    @Override
    public void applyTickEffect(Player player, int level, int slot, float efficiency) {
        if (player.level().isClientSide) return;
        UUID playerId = player.getUUID();
        FoodData foodData = player.getFoodData();
        int currentFood = foodData.getFoodLevel();
        int lastFood = LAST_FOOD_LEVEL.getOrDefault(playerId, currentFood);

        if (currentFood < lastFood) {
            if (RANDOM.nextFloat() < getPreventChance(level) * efficiency) {
                foodData.setFoodLevel(lastFood);
                long now = System.currentTimeMillis();
                List<Long> timestamps = PREVENTION_TIMESTAMPS.computeIfAbsent(playerId, k -> new ArrayList<>());
                timestamps.add(now);
                timestamps.removeIf(t -> now - t > 60_000);
                if (timestamps.size() >= 3 && player instanceof ServerPlayer sp) {
                    ModAdvancementTriggers.HUNGER_DRAIN_PREVENTED_5(sp);
                    timestamps.clear();
                }
            }
        }
        LAST_FOOD_LEVEL.put(playerId, foodData.getFoodLevel());
    }

    @Override
    public void removeEffect(Player player) {
        UUID playerId = player.getUUID();
        LAST_FOOD_LEVEL.remove(playerId);
        PREVENTION_TIMESTAMPS.remove(playerId);
    }

    private float getPreventChance(int level) {
        return switch (level) {
            case 1  -> 0.03f; case 2  -> 0.05f; case 3  -> 0.08f;
            case 4  -> 0.10f; case 5  -> 0.13f; case 6  -> 0.16f;
            case 7  -> 0.19f; case 8  -> 0.22f; case 9  -> 0.25f;
            case 10 -> 0.28f; case 11 -> 0.31f; case 12 -> 0.35f;
            default -> 0.03f;
        };
    }

    public float getSaturationBonus(int level) {
        return level >= 12 ? 0.15f : 0.0f;
    }
}
