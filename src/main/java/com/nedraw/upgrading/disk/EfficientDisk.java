package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class EfficientDisk extends UpgradeDisk {

    private static final Random RANDOM = new Random();
    private static final Map<UUID, Map<Integer, Integer>> LAST_DURABILITY = new HashMap<>();

    // Track timestamps of successful durability saves for the 5-in-1-minute check
    private static final Map<UUID, List<Long>> SAVE_TIMESTAMPS = new HashMap<>();

    public EfficientDisk() {
        super("efficient", "Efficient", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level) {
        LAST_DURABILITY.putIfAbsent(player.getUUID(), new HashMap<>());
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        if (player.level().isClientSide) return;

        UUID playerId = player.getUUID();
        Map<Integer, Integer> playerDurability = LAST_DURABILITY.computeIfAbsent(playerId, k -> new HashMap<>());

        int mainHandSlot = player.getInventory().selected;
        boolean savedThisTick = false;

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);

            if (stack.isEmpty() || !stack.isDamageableItem()) continue;

            int currentDamage = stack.getDamageValue();
            int lastDamage = playerDurability.getOrDefault(slot, currentDamage);

            if (currentDamage > lastDamage) {
                boolean isMainHand = (slot == mainHandSlot);

                if (isMainHand) {
                    if (RANDOM.nextFloat() < getToolPreventChance(level)) {
                        stack.setDamageValue(lastDamage);
                        savedThisTick = true;
                    }
                } else {
                    if (level >= 12 && RANDOM.nextFloat() < 0.06f) {
                        stack.setDamageValue(lastDamage);
                        savedThisTick = true;
                    }
                }
            }

            playerDurability.put(slot, stack.getDamageValue());
        }

        // Track save and check for advancement
        if (savedThisTick) {
            long now = System.currentTimeMillis();
            List<Long> timestamps = SAVE_TIMESTAMPS.computeIfAbsent(playerId, k -> new ArrayList<>());
            timestamps.add(now);

            timestamps.removeIf(t -> now - t > 60_000);

            if (timestamps.size() >= 4 && player instanceof ServerPlayer sp) {
                ModAdvancementTriggers.DURABILITY_SAVED_5(sp);
                timestamps.clear(); // Reset to prevent spam
            }
        }
    }

    @Override
    public void removeEffect(Player player) {
        UUID playerId = player.getUUID();
        LAST_DURABILITY.remove(playerId);
        SAVE_TIMESTAMPS.remove(playerId);
    }

    private float getToolPreventChance(int level) {
        return switch (level) {
            case 1  -> 0.03f;
            case 2  -> 0.04f;
            case 3  -> 0.05f;
            case 4  -> 0.06f;
            case 5  -> 0.07f;
            case 6  -> 0.09f;
            case 7  -> 0.11f;
            case 8  -> 0.13f;
            case 9  -> 0.15f;
            case 10 -> 0.17f;
            case 11 -> 0.19f;
            case 12 -> 0.22f;
            default -> 0.03f;
        };
    }
}