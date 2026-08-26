package com.nedraw.upgrading.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.*;

public class PlayerDiskData implements INBTSerializable<CompoundTag> {

    private final Set<String> unlockedDisks = new HashSet<>();
    private final Map<String, Integer> diskLevels = new HashMap<>();
    private final String[] equippedSlots = new String[3];
    private final Map<String, Long> abilityCooldowns = new HashMap<>();
    private final ItemStack[] zSlots = new ItemStack[]{ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

    private float pittyMeter = 0.0f;

    public static PlayerDiskData get(Player player) {
        return player.getData(ModAttachments.PLAYER_DISK_DATA);
    }

    public float getPittyMeter() {
        return pittyMeter;
    }

    public void updatePittyMeter(com.nedraw.upgrading.disk.DiskRarity rarity) {
        switch (rarity) {
            case BASIC     -> pittyMeter += 0.032f;
            case RARE      -> pittyMeter += 0.015f;
            case EPIC      -> pittyMeter -= 0.125f;
            case LEGENDARY -> pittyMeter -= 0.30f;
            case MYTHIC    -> pittyMeter  -= 0.85f;
        }
        //-0.85
        pittyMeter = Math.max(-0.75f, Math.min(1.0f, pittyMeter));
    }

    public long getAbilityCooldown(String diskId) {
        return abilityCooldowns.getOrDefault(diskId, 0L);
    }

    public void setAbilityCooldown(String diskId, long timestamp) {
        abilityCooldowns.put(diskId, timestamp);
    }

    public boolean isDiskUnlocked(String diskId) {
        return unlockedDisks.contains(diskId);
    }

    public void unlockDisk(String diskId) {
        if (!unlockedDisks.contains(diskId)) {
            unlockedDisks.add(diskId);
            var disk = com.nedraw.upgrading.disk.DiskRegistry.getDisk(diskId);
            if (disk != null) {
                diskLevels.put(diskId, disk.getRarity().getStartLevel());
            } else {
                diskLevels.put(diskId, 1);
            }
        }
    }

    public Set<String> getUnlockedDisks() {
        return Collections.unmodifiableSet(unlockedDisks);
    }

    public int getDiskLevel(String diskId) {
        return diskLevels.getOrDefault(diskId, 1);
    }

    public void setDiskLevel(String diskId, int level) {
        if (unlockedDisks.contains(diskId)) {
            diskLevels.put(diskId, Math.min(level, 12));
        }
    }

    public boolean upgradeDisk(String diskId) {
        if (!unlockedDisks.contains(diskId)) return false;
        int currentLevel = getDiskLevel(diskId);
        if (currentLevel >= 12) return false;
        setDiskLevel(diskId, currentLevel + 1);
        return true;
    }

    public String getEquippedDisk(int slot) {
        if (slot < 0 || slot >= 3) return null;
        return equippedSlots[slot];
    }

    public boolean equipDisk(String diskId, int slot) {
        if (slot < 0 || slot >= 3) return false;
        if (!unlockedDisks.contains(diskId)) return false;
        for (int i = 0; i < 3; i++) {
            if (i != slot && diskId.equals(equippedSlots[i])) return false;
        }
        equippedSlots[slot] = diskId;
        return true;
    }

    public void unequipSlot(int slot) {
        if (slot >= 0 && slot < 3) equippedSlots[slot] = null;
    }

    public boolean isDiskEquipped(String diskId) {
        for (String equipped : equippedSlots) {
            if (diskId != null && diskId.equals(equipped)) return true;
        }
        return false;
    }

    public String[] getEquippedDisks() {
        return equippedSlots.clone();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        ListTag unlockedList = new ListTag();
        for (String diskId : unlockedDisks) {
            unlockedList.add(StringTag.valueOf(diskId));
        }
        tag.put("UnlockedDisks", unlockedList);

        CompoundTag levelsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : diskLevels.entrySet()) {
            levelsTag.putInt(entry.getKey(), entry.getValue());
        }
        tag.put("DiskLevels", levelsTag);

        CompoundTag equippedTag = new CompoundTag();
        for (int i = 0; i < 3; i++) {
            if (equippedSlots[i] != null) equippedTag.putString("Slot" + i, equippedSlots[i]);
        }
        tag.put("EquippedSlots", equippedTag);

        CompoundTag cooldownsTag = new CompoundTag();
        for (Map.Entry<String, Long> entry : abilityCooldowns.entrySet()) {
            cooldownsTag.putLong(entry.getKey(), entry.getValue());
        }
        tag.put("AbilityCooldowns", cooldownsTag);

        tag.putFloat("PittyMeter", pittyMeter);

        CompoundTag zSlotsTag = new CompoundTag();
        for (int i = 0; i < 3; i++) {
            if (!zSlots[i].isEmpty()) {
                zSlotsTag.put("ZSlot" + i, zSlots[i].save(provider));
            }
        }
        tag.put("ZSlots", zSlotsTag);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        unlockedDisks.clear();
        ListTag unlockedList = tag.getList("UnlockedDisks", Tag.TAG_STRING);
        for (int i = 0; i < unlockedList.size(); i++) {
            unlockedDisks.add(unlockedList.getString(i));
        }

        diskLevels.clear();
        CompoundTag levelsTag = tag.getCompound("DiskLevels");
        for (String key : levelsTag.getAllKeys()) {
            diskLevels.put(key, levelsTag.getInt(key));
        }

        Arrays.fill(equippedSlots, null);
        CompoundTag equippedTag = tag.getCompound("EquippedSlots");
        for (int i = 0; i < 3; i++) {
            String key = "Slot" + i;
            if (equippedTag.contains(key)) equippedSlots[i] = equippedTag.getString(key);
        }

        abilityCooldowns.clear();
        CompoundTag cooldownsTag = tag.getCompound("AbilityCooldowns");
        for (String key : cooldownsTag.getAllKeys()) {
            abilityCooldowns.put(key, cooldownsTag.getLong(key));
        }

        pittyMeter = tag.contains("PittyMeter") ? tag.getFloat("PittyMeter") : 0.0f;

        Arrays.fill(zSlots, ItemStack.EMPTY);
        CompoundTag zSlotsTag = tag.getCompound("ZSlots");
        for (int i = 0; i < 3; i++) {
            String key = "ZSlot" + i;
            if (zSlotsTag.contains(key)) {
                zSlots[i] = ItemStack.parseOptional(provider, zSlotsTag.getCompound(key));
            }
        }
    }

    public int getTotalXP(Player player) {
        int points = 0;
        int level = player.experienceLevel;
        if (level >= 32) {
            points = (int) (4.5 * level * level - 162.5 * level + 2220);
        } else if (level >= 17) {
            points = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            points = level * level + 6 * level;
        }
        points += (int) (player.experienceProgress * player.getXpNeededForNextLevel());
        return points;
    }

    public boolean hasEnoughXP(Player player, int cost) {
        return getTotalXP(player) >= cost;
    }

    public void consumeXP(Player player, int amount) {
        int remaining = getTotalXP(player) - amount;
        player.experienceLevel = 0;
        player.experienceProgress = 0.0f;
        player.totalExperience = 0;
        player.giveExperiencePoints(remaining);
    }

    public ItemStack getZSlot(int slot) {
        if (slot < 0 || slot >= 3) return ItemStack.EMPTY;
        return zSlots[slot];
    }

    public void setZSlot(int slot, ItemStack stack) {
        if (slot < 0 || slot >= 3) return;
        zSlots[slot] = stack == null ? ItemStack.EMPTY : stack;
    }

    public void removeDisk(String diskId) {
        unlockedDisks.remove(diskId);
        diskLevels.remove(diskId);
        for (int i = 0; i < 3; i++) {
            if (diskId.equals(equippedSlots[i])) equippedSlots[i] = null;
        }
    }
}