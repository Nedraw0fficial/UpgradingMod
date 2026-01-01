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

    // Track equipped disks with their levels per player
    private static final Map<UUID, Map<String, Integer>> LAST_ACTIVE_DISKS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Only run on server side
        if (player.level().isClientSide) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);
        UUID playerId = player.getUUID();

        // Get current equipped disks with levels
        Map<String, Integer> currentDisks = new HashMap<>();
        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null) {
                int level = diskData.getDiskLevel(diskId);
                currentDisks.put(diskId, level);
            }
        }

        // Get last tick's state
        Map<String, Integer> lastDisks = LAST_ACTIVE_DISKS.getOrDefault(playerId, new HashMap<>());

        // Apply effects for new disks or changed levels
        for (Map.Entry<String, Integer> entry : currentDisks.entrySet()) {
            String diskId = entry.getKey();
            int currentLevel = entry.getValue();
            Integer lastLevel = lastDisks.get(diskId);

            // Apply if disk is new OR level changed
            if (lastLevel == null || lastLevel != currentLevel) {
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk != null) {
                    // Remove old effect first if it existed
                    if (lastLevel != null) {
                        disk.removeEffect(player);
                    }
                    // Apply new effect
                    disk.applyEffect(player, currentLevel);
                }
            }

            // IMPORTANT: For level 12+ disks, call applyEffect every tick
            // This is needed for tick-based abilities like dash detection
            // The disk itself optimizes to only update attributes when needed
            else if (currentLevel >= 12) {
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk != null) {
                    disk.applyEffect(player, currentLevel);
                }
            }
        }

        // Remove effects from unequipped disks
        for (String diskId : lastDisks.keySet()) {
            if (!currentDisks.containsKey(diskId)) {
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk != null) {
                    disk.removeEffect(player);
                }
            }
        }

        // Update tracking
        LAST_ACTIVE_DISKS.put(playerId, currentDisks);
    }
}