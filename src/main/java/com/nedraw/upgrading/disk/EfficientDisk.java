package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class EfficientDisk extends UpgradeDisk {

    private static final Random RANDOM = new Random();

    // Track durability of held items per player
    private static final Map<UUID, Map<Integer, Integer>> LAST_DURABILITY = new HashMap<>();

    public EfficientDisk() {
        super("efficient", "Efficient", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level) {
        // Initialize tracking
        LAST_DURABILITY.putIfAbsent(player.getUUID(), new HashMap<>());
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        // Server-side only
        if (player.level().isClientSide) return;

        UUID playerId = player.getUUID();
        Map<Integer, Integer> playerDurability = LAST_DURABILITY.get(playerId);
        if (playerDurability == null) {
            playerDurability = new HashMap<>();
            LAST_DURABILITY.put(playerId, playerDurability);
        }

        // Get the held item slot
        int mainHandSlot = player.getInventory().selected; // Hotbar slot (0-8)

        // Check all inventory slots for tools/armor
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);

            if (stack.isEmpty() || !stack.isDamageableItem()) {
                continue;
            }

            int currentDamage = stack.getDamageValue();
            int lastDamage = playerDurability.getOrDefault(slot, currentDamage);

            // Check if durability decreased (damage increased)
            if (currentDamage > lastDamage) {
                boolean isMainHand = (slot == mainHandSlot);

                // Determine which chance to use
                if (isMainHand) {
                    // MAIN HAND TOOL: Use tool durability prevention chance
                    float toolPreventChance = getToolPreventChance(level);
                    if (RANDOM.nextFloat() < toolPreventChance) {
                        stack.setDamageValue(lastDamage);
                    }
                } else {
                    // ARMOR/OFFHAND: Level 12 bonus only (7% chance)
                    if (level >= 12 && RANDOM.nextFloat() < 0.07f) {
                        stack.setDamageValue(lastDamage);
                    }
                }
            }

            // Update tracking with current damage value
            playerDurability.put(slot, stack.getDamageValue());
        }
    }

    @Override
    public void removeEffect(Player player) {
        LAST_DURABILITY.remove(player.getUUID());
    }

    private float getToolPreventChance(int level) {
        return switch (level) {
            case 1 -> 0.05f;   // 5%
            case 2 -> 0.06f;   // 6%
            case 3 -> 0.07f;   // 7%
            case 4 -> 0.08f;   // 8%
            case 5 -> 0.10f;   // 10%
            case 6 -> 0.12f;   // 12%
            case 7 -> 0.14f;   // 14%
            case 8 -> 0.16f;   // 16%
            case 9 -> 0.18f;   // 18%
            case 10 -> 0.20f;  // 20%
            case 11 -> 0.22f;  // 22%
            case 12 -> 0.24f;  // 24%
            default -> 0.05f;
        };
    }
}