package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.item.ZSlotItem;
import com.nedraw.upgrading.disk.BlueprintDisk;
import com.nedraw.upgrading.disk.DiskRarity;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ZSlotEffects {

    public static float[] calculateBaseEfficiencyMultipliers(Player player) {
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
                int roseGoldCount = 0;
                for (int s = 0; s < 3; s++) {
                    ItemStack otherZSlot = data.getZSlot(s);
                    if (!otherZSlot.isEmpty() && "rose_gold".equals(ZSlotItem.getFrame(otherZSlot))) {
                        roseGoldCount++;
                    }
                }
                modifiers[slot] += roseGoldCount * 0.09f;
            }
        }

        float[] result = new float[3];
        for (int slot = 0; slot < 3; slot++) {
            result[slot] = base[slot] + modifiers[slot];
        }

        return result;
    }

    public static float[] calculateAllEfficiencyMultipliers(Player player) {
        PlayerDiskData data = PlayerDiskData.get(player);
        float[] result = calculateBaseEfficiencyMultipliers(player);
        float[] withBlueprint = result.clone();

        for (int slot = 0; slot < 2; slot++) {
            if (!"blueprint".equals(data.getEquippedDisk(slot))) continue;
            if (!isBlueprintCompatible(data, slot)) continue;

            int blueprintLevel = data.getDiskLevel("blueprint");
            withBlueprint[slot + 1] += result[slot] * BlueprintDisk.getCopyEfficiency(blueprintLevel);
        }

        return withBlueprint;
    }

    public static float getBaseEfficiencyMultiplier(Player player, int slot) {
        return calculateBaseEfficiencyMultipliers(player)[slot];
    }

    public static float getBlueprintBonus(Player player, int slot) {
        float[] all = calculateAllEfficiencyMultipliers(player);
        float[] base = calculateBaseEfficiencyMultipliers(player);
        return Math.max(0.0f, all[slot] - base[slot]);
    }

    public static boolean isBlueprintCompatible(PlayerDiskData data, int blueprintSlot) {
        if (blueprintSlot >= 2) return false;

        String target = data.getEquippedDisk(blueprintSlot + 1);
        if (target == null || target.equals("blueprint")) return false;

        UpgradeDisk disk = DiskRegistry.getDisk(target);
        return disk != null && disk.getRarity() != DiskRarity.MYTHIC;
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
            case "cactus"   -> 0.26f;
            case "glass"    -> {
                int emptySlots = 0;
                for (int s = 0; s < 3; s++) {
                    if (s != slot && data.getEquippedDisk(s) == null) emptySlots++;
                }
                yield 0.09f + (emptySlots * 0.08f);
            }
            case "rose_gold" -> 0.00f;
            case "sponge"    -> diskLevel * 0.021f;
            case "mushroom"  -> {
                String equippedId = data.getEquippedDisk(slot);
                if (equippedId == null) yield 0.15f;
                var disk = com.nedraw.upgrading.disk.DiskRegistry.getDisk(equippedId);
                if (disk == null) yield 0.15f;
                int tier = disk.getRarity().ordinal(); // BASIC=0, RARE=1, EPIC=2, LEGENDARY=3, MYTHIC=4
                yield 0.25f - (tier * 0.04f);
            }
            case "void" -> 0.00f;
            default     -> 0.00f;
        };
    }

    public static float getEfficiencyMultiplier(Player player, int slot) {
        return calculateAllEfficiencyMultipliers(player)[slot];
    }
}