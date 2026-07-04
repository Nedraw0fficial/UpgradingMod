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
        return level >= 12 ? 120_000L : 90_000L;
    }

    @Override
    public boolean isAnimated() {
        return true;
    }

    @Override
    public int getFrameCount() {
        return 26;
    }

    @Override
    public int getTicksPerFrame() {
        return 4;
    }

    @Override
    public int getFrameSize() {
        return 64;
    }
}