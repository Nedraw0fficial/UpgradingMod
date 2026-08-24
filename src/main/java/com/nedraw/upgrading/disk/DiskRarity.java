package com.nedraw.upgrading.disk;

import net.minecraft.network.chat.Component;

public enum DiskRarity {
    BASIC(1, 2, 1.8, 0x8CC8CF),
    RARE(4, 6, 2.0, 0xFFA538),
    EPIC(7, 25, 2.2, 0xA142C9),
    LEGENDARY(9, 100, 2.5, 0xD3F224),
    MYTHIC(11, 649, 3.0, 0x2A139E);

    private final int startLevel;
    private final int baseXpCost;
    private final int color;
    private final double xpMultiplier;

    DiskRarity(int startLevel, int baseXpCost, double xpMultiplier, int color) {
        this.startLevel = startLevel;
        this.baseXpCost = baseXpCost;
        this.xpMultiplier = xpMultiplier;
        this.color = color;
    }

    public int getStartLevel() {
        return startLevel;
    }

    public int getXpCostForLevel(int currentLevel) {
        int levelsFromStart = currentLevel - startLevel;
        return (int) (baseXpCost * Math.pow(xpMultiplier, levelsFromStart));
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