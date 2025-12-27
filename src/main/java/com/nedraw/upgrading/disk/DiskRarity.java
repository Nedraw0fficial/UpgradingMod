package com.nedraw.upgrading.disk;

public enum DiskRarity {
    BASIC(1, 5, 0x8CC8CF),      // Blue-Gray
    RARE(4, 10, 0xFFA538),       // Orange
    EPIC(7, 20, 0xA142C9),       // Purple
    LEGENDARY(9, 40, 0xD3F224),  // Lime-Yellow
    MYTHIC(11, 80, 0x2A139E);    // Deep Purplish-Blue

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
        // Exponential scaling: cost doubles each level
        int levelsFromStart = currentLevel - startLevel;
        return baseXpCost * (int) Math.pow(2, levelsFromStart);
    }

    public int getColor() {
        return color;
    }
}