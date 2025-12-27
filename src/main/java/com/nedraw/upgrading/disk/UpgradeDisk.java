package com.nedraw.upgrading.disk;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import java.util.ArrayList;
import java.util.List;

public class UpgradeDisk {
    private final String id;
    private final String displayName;
    private final DiskRarity rarity;
    private final List<String> descriptions; // One for each level (1-12)

    public UpgradeDisk(String id, String displayName, DiskRarity rarity) {
        this.id = id;
        this.displayName = displayName;
        this.rarity = rarity;
        this.descriptions = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public DiskRarity getRarity() {
        return rarity;
    }

    // Add descriptions for each level
    public UpgradeDisk withDescription(int level, String description) {
        while (descriptions.size() < level) {
            descriptions.add("");
        }
        descriptions.set(level - 1, description);
        return this;
    }

    public String getDescriptionForLevel(int level) {
        if (level < 1 || level > descriptions.size()) {
            return "No description";
        }
        return descriptions.get(level - 1);
    }

    // This will be called every tick for equipped disks
    public void applyEffect(Player player, int level) {
        // Override this in specific disk implementations
    }

    // Check if this disk can be upgraded
    public boolean canUpgrade(int currentLevel) {
        return currentLevel < 12;
    }

    public int getMaxLevel() {
        return 12;
    }
}