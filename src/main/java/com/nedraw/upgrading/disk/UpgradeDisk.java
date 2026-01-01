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

    // Called when disk is equipped OR when level changes
    // Use this for one-time setup (attributes, etc.)
    public void applyEffect(Player player, int level) {
        // Override this in specific disk implementations
    }

    // Called EVERY TICK for continuous effects (magnet pull, air bonus, dash detection, etc.)
    // Only override this if your disk needs tick-based logic
    // Leave empty by default for performance
    public void applyTickEffect(Player player, int level) {
        // Override this in specific disk implementations that need tick updates
    }

    // Called when disk is unequipped
    public void removeEffect(Player player) {
        // Override in specific disks to remove their effects
    }

    // Check if this disk can be upgraded
    public boolean canUpgrade(int currentLevel) {
        return currentLevel < 12;
    }

    public int getMaxLevel() {
        return 12;
    }
}