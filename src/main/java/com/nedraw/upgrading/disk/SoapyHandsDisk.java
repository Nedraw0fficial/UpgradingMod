package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoapyHandsDisk extends UpgradeDisk {

    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    public SoapyHandsDisk() {
        super("soapy_hands", "Soapy Hands", DiskRarity.EPIC);
    }

    @Override
    public void applyEffect(Player player, int level) {
        APPLIED_LEVELS.put(player.getUUID(), level);
    }

    @Override
    public void removeEffect(Player player) {
        APPLIED_LEVELS.remove(player.getUUID());
    }

    public float getDropChance(int level, float efficiency) {
        float base = level < 12 ? ((level - 6) * 2) / 100.0f : 0.15f;
        return Math.min(base * efficiency, 0.80f);
    }

    public float getDropChance(int level) { return getDropChance(level, 1.0f); }

    public boolean canDropArmor(int level) { return level >= 12; }

    public int getAppliedLevel(UUID playerId) {
        return APPLIED_LEVELS.getOrDefault(playerId, 0);
    }
}
