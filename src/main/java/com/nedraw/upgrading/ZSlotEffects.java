package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.item.ZSlotItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ZSlotEffects {

    public static float[] calculateAllEfficiencyMultipliers(Player player) {
        PlayerDiskData data = PlayerDiskData.get(player);
        float[] base = new float[3];
        float[] modifiers = new float[3];

        for (int slot = 0; slot < 3; slot++) {
            ItemStack zSlot = data.getZSlot(slot);
            if (zSlot.isEmpty()) {
                base[slot] = 1.0f;
                continue;
            }

            String frame = ZSlotItem.getFrame(zSlot);
            String equippedDisk = data.getEquippedDisk(slot);
            int level = equippedDisk != null ? data.getDiskLevel(equippedDisk) : 0;

            base[slot] = 1.0f + getFrameBonus(frame, player, slot, level, data);
        }

        for (int slot = 0; slot < 3; slot++) {
            ItemStack zSlot = data.getZSlot(slot);
            if (zSlot.isEmpty()) continue;

            String frame = ZSlotItem.getFrame(zSlot);

            if (frame.equals("cactus")) {
                for (int other = 0; other < 3; other++) {
                    if (other != slot) modifiers[other] -= 0.06f;
                }
            }

            if (frame.equals("rose_gold")) {
                for (int other = 0; other < 3; other++) {
                    if (other != slot) {
                        ItemStack otherZSlot = data.getZSlot(other);
                        if (!otherZSlot.isEmpty() && "rose_gold".equals(ZSlotItem.getFrame(otherZSlot))) {
                            modifiers[slot] += 0.07f;
                        }
                    }
                }
            }
        }

        float[] result = new float[3];
        for (int slot = 0; slot < 3; slot++) {
            result[slot] = base[slot] + modifiers[slot];
        }

        return result;
    }

    private static float getFrameBonus(String frame, Player player, int slot,
                                       int diskLevel, PlayerDiskData data) {
        return switch (frame) {
            case "fabric"   -> 0.02f;
            case "wooden"   -> 0.05f;
            case "copper"   -> 0.08f;
            case "iron"     -> 0.12f;
            case "golden"   -> 0.16f;
            case "amethyst" -> 0.20f;
            case "cactus"   -> 0.18f;
            case "glass"    -> {
                int emptySlots = 0;
                for (int s = 0; s < 3; s++) {
                    if (s != slot && data.getEquippedDisk(s) == null) emptySlots++;
                }
                yield 0.10f + (emptySlots * 0.05f);
            }
            case "rose_gold" -> 0.00f;
            case "sponge"    -> diskLevel * 0.01f;
            case "mushroom"  -> {
                String equippedId = data.getEquippedDisk(slot);
                if (equippedId == null) yield 0.15f;
                var disk = com.nedraw.upgrading.disk.DiskRegistry.getDisk(equippedId);
                if (disk == null) yield 0.15f;
                int tier = disk.getRarity().ordinal(); // BASIC=0, RARE=1, EPIC=2, LEGENDARY=3, MYTHIC=4
                yield 0.15f - (tier * 0.03f);
            }
            case "void" -> 0.00f;
            default     -> 0.00f;
        };
    }

    public static float getEfficiencyMultiplier(Player player, int slot) {
        return calculateAllEfficiencyMultipliers(player)[slot];
    }
}