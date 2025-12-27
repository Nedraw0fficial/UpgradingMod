package com.nedraw.upgrading.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.*;

public class PlayerDiskData implements INBTSerializable<CompoundTag> {
    // Storage
    private final Set<String> unlockedDisks = new HashSet<>();
    private final Map<String, Integer> diskLevels = new HashMap<>();
    private final String[] equippedSlots = new String[3]; // 3 equipment slots

    // Get data from player
    public static PlayerDiskData get(Player player) {
        return player.getData(ModAttachments.PLAYER_DISK_DATA);
    }

    // === DISK UNLOCKING ===

    public boolean isDiskUnlocked(String diskId) {
        return unlockedDisks.contains(diskId);
    }

    public void unlockDisk(String diskId) {
        if (!unlockedDisks.contains(diskId)) {
            unlockedDisks.add(diskId);
            // Start at level 1 by default (we'll adjust based on rarity later)
            diskLevels.put(diskId, 1);
        }
    }

    public Set<String> getUnlockedDisks() {
        return Collections.unmodifiableSet(unlockedDisks);
    }

    // === DISK LEVELS ===

    public int getDiskLevel(String diskId) {
        return diskLevels.getOrDefault(diskId, 1);
    }

    public void setDiskLevel(String diskId, int level) {
        if (unlockedDisks.contains(diskId)) {
            diskLevels.put(diskId, Math.min(level, 12)); // Max level 12
        }
    }

    public boolean upgradeDisk(String diskId) {
        if (!unlockedDisks.contains(diskId)) return false;

        int currentLevel = getDiskLevel(diskId);
        if (currentLevel >= 12) return false;

        setDiskLevel(diskId, currentLevel + 1);
        return true;
    }

    // === EQUIPMENT SLOTS ===

    public String getEquippedDisk(int slot) {
        if (slot < 0 || slot >= 3) return null;
        return equippedSlots[slot];
    }

    public boolean equipDisk(String diskId, int slot) {
        if (slot < 0 || slot >= 3) return false;
        if (!unlockedDisks.contains(diskId)) return false;

        // Check if disk is already equipped in another slot
        for (int i = 0; i < 3; i++) {
            if (i != slot && diskId.equals(equippedSlots[i])) {
                return false; // Already equipped elsewhere
            }
        }

        equippedSlots[slot] = diskId;
        return true;
    }

    public void unequipSlot(int slot) {
        if (slot >= 0 && slot < 3) {
            equippedSlots[slot] = null;
        }
    }

    public boolean isDiskEquipped(String diskId) {
        for (String equipped : equippedSlots) {
            if (diskId != null && diskId.equals(equipped)) {
                return true;
            }
        }
        return false;
    }

    public String[] getEquippedDisks() {
        return equippedSlots.clone();
    }

    // === NBT SERIALIZATION ===

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        // Save unlocked disks
        ListTag unlockedList = new ListTag();
        for (String diskId : unlockedDisks) {
            unlockedList.add(StringTag.valueOf(diskId));
        }
        tag.put("UnlockedDisks", unlockedList);

        // Save disk levels
        CompoundTag levelsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : diskLevels.entrySet()) {
            levelsTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("DiskLevels", levelsTag);

        // Save equipped slots
        CompoundTag equippedTag = new CompoundTag();
        for (int i = 0; i < 3; i++) {
            if (equippedSlots[i] != null) {
                equippedTag.putString("Slot" + i, equippedSlots[i]);
            }
        }
        tag.put("EquippedSlots", equippedTag);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        // Load unlocked disks
        unlockedDisks.clear();
        ListTag unlockedList = tag.getList("UnlockedDisks", Tag.TAG_STRING);
        for (int i = 0; i < unlockedList.size(); i++) {
            unlockedDisks.add(unlockedList.getString(i));
        }

        // Load disk levels
        diskLevels.clear();
        CompoundTag levelsTag = tag.getCompound("DiskLevels");
        for (String key : levelsTag.getAllKeys()) {
            diskLevels.put(key, levelsTag.getInt(key));
        }

        // Load equipped slots
        Arrays.fill(equippedSlots, null);
        CompoundTag equippedTag = tag.getCompound("EquippedSlots");
        for (int i = 0; i < 3; i++) {
            String key = "Slot" + i;
            if (equippedTag.contains(key)) {
                equippedSlots[i] = equippedTag.getString(key);
            }
        }
    }
}