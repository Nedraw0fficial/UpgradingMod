package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.NecroArcherHandler;
import com.nedraw.upgrading.ZSlotEffects;
import com.nedraw.upgrading.data.PlayerDiskData;
import net.minecraft.world.entity.player.Player;

public class NecroArcherDisk extends UpgradeDisk {

    public NecroArcherDisk() {
        super("necro_archer", "Necro-Archer", DiskRarity.MYTHIC);
    }

    @Override
    public void activateAbility(Player player, int level) {
        PlayerDiskData data = PlayerDiskData.get(player);
        float efficiency = 1.0f;
        for (int slot = 0; slot < 3; slot++) {
            String diskId = data.getEquippedDisk(slot);
            if ("necro_archer".equals(diskId)) {
                efficiency = ZSlotEffects.getEfficiencyMultiplier(player, slot);
                break;
            }
        }
        NecroArcherHandler.activateBoost(player, level, efficiency);
    }

    @Override
    public long getAbilityCooldownMs(int level) {
        // Base cooldown — efficiency reduction is applied in ActivateMythicPacket
        return level >= 12 ? 120_000L : 90_000L;
    }

    @Override
    public boolean isAnimated() { return true; }

    @Override
    public int getFrameCount() { return 26; }

    @Override
    public int getTicksPerFrame() { return 5; }

    @Override
    public int getFrameSize() { return 64; }
}
