package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class PlayerTickHandler {

    private static final Map<UUID, String[]> LAST_DISK_IDS   = new HashMap<>();
    private static final Map<UUID, int[]>    LAST_DISK_LEVELS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);
        UUID playerId = player.getUUID();

        String[] lastIds   = LAST_DISK_IDS.computeIfAbsent(playerId, k -> new String[3]);
        int[]    lastLevels = LAST_DISK_LEVELS.computeIfAbsent(playerId, k -> new int[3]);

        float[] efficiencies = ZSlotEffects.calculateAllEfficiencyMultipliers(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            String lastId = lastIds[slot];
            int currentLevel = diskId != null ? diskData.getDiskLevel(diskId) : 0;
            int lastLevel = lastLevels[slot];
            float efficiency = efficiencies[slot];

            if (diskId == null && lastId != null) {
                UpgradeDisk oldDisk = DiskRegistry.getDisk(lastId);
                if (oldDisk != null) oldDisk.removeEffect(player);
                lastIds[slot] = null;
                lastLevels[slot] = 0;
                continue;
            }

            if (diskId == null) continue;

            UpgradeDisk disk = DiskRegistry.getDisk(diskId);
            if (disk == null) continue;

            if (!diskId.equals(lastId) || currentLevel != lastLevel) {
                if (lastId != null && !lastId.equals(diskId)) {
                    UpgradeDisk oldDisk = DiskRegistry.getDisk(lastId);
                    if (oldDisk != null) oldDisk.removeEffect(player);
                }
                disk.applyEffect(player, currentLevel, slot, efficiency);
                lastIds[slot] = diskId;
                lastLevels[slot] = currentLevel;
            }

            disk.applyTickEffect(player, currentLevel, slot, efficiency);
        }
    }
}