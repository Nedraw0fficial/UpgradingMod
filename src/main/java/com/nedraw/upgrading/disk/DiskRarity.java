package com.nedraw.upgrading.disk;

import net.minecraft.network.chat.Component;

public enum DiskRarity {
    BASIC(1, 3, 0x8CC8CF),         // Blue-Gray
    RARE(4, 10, 0xFFA538),          // Orange
    EPIC(7, 47, 0xA142C9),         // Purple
    LEGENDARY(9, 191, 0xD3F224),   // Lime-Yellow
    MYTHIC(11, 1017, 0x2A139E);    // Deep Purplish-Blue

    private final int startLevel;
    private final int baseXpCost;
    private final int color;

    DiskRarity(int startLevel, int baseXpCost, int color) {
        this.startLevel = startLevel;
        this.baseXpCost = baseXpCost;
        this.color = color;
    }

    public int getStartLevel() {
        return startLevel;
    }

    public int getXpCostForLevel(int currentLevel) {
        int levelsFromStart = currentLevel - startLevel;
        return (int) (baseXpCost * Math.pow(1.7, levelsFromStart));
    }

    public int getColor() {
        return color;
    }

    // New string method
    public String getDisplayName() {
        // Translation key format (FINALLY !)
        return Component.translatable("rarity.upgrading." + this.name().toLowerCase()).getString();
    }
}