package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.NecroArcherHandler;
import net.minecraft.world.entity.player.Player;

public class NecroArcherDisk extends UpgradeDisk {

    public NecroArcherDisk() {
        super("necro_archer", "Necro-Archer", DiskRarity.MYTHIC);
    }

    @Override
    public void activateAbility(Player player, int level) {
        NecroArcherHandler.activateBoost(player, level);
    }

    @Override
    public long getAbilityCooldownMs(int level) {
        // Cooldown starts AFTER the boost ends
        // L11: boost 7s + 90s cooldown = 97s total cycle
        // L12: boost 10s + 120s cooldown = 130s total cycle
        return level >= 12 ? 120_000L : 90_000L;
    }
}