package com.nedraw.upgrading.disk;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import java.util.ArrayList;
import java.util.List;

public class UpgradeDisk {
    private final String id;
    private final String displayName;
    private final DiskRarity rarity;
    private final List<String> descriptions;

    public UpgradeDisk(String id, String displayName, DiskRarity rarity) {
        this.id = id;
        this.displayName = displayName;
        this.rarity = rarity;
        this.descriptions = new ArrayList<>();
    }

    public String getId() { return id; }

    public String getDisplayName() {
        return Component.translatable("disk.upgrading." + id).getString();
    }

    public DiskRarity getRarity() { return rarity; }

    public UpgradeDisk withDescription(int level, String description) {
        while (descriptions.size() < level) descriptions.add("");
        descriptions.set(level - 1, description);
        return this;
    }

    public String getDescriptionForLevel(int level) {
        if (level < 1 || level > 12) return "No description";
        String translationKey = "description.upgrading." + id + "." + level;
        Component translated = Component.translatable(translationKey);
        String result = translated.getString();
        if (!result.equals(translationKey)) return result;
        if (level <= descriptions.size() && !descriptions.get(level - 1).isEmpty())
            return descriptions.get(level - 1);
        return "No description";
    }

    public void applyEffect(Player player, int level, int slot, float efficiency) {
        applyEffect(player, level); // backwards compat
    }

    public void applyEffect(Player player, int level) {}

    public void applyTickEffect(Player player, int level, int slot, float efficiency) {
        applyTickEffect(player, level); // backwards compat
    }

    public void applyTickEffect(Player player, int level) {}

    public void removeEffect(Player player) {}

    public boolean canUpgrade(int currentLevel) { return currentLevel < 12; }

    public int getMaxLevel() { return 12; }

    public void activateAbility(Player player, int level) {}

    public long getAbilityCooldownMs(int level) { return 60000; }

    public boolean isAnimated() { return false; }

    public int getFrameCount() { return 1; }

    public int getTicksPerFrame() { return 2; }

    public int getFrameSize() { return 48; }
}