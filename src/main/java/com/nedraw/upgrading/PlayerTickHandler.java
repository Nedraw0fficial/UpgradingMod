package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class PlayerTickHandler {

    // Track which disks were active last tick
    private static final java.util.Map<java.util.UUID, Set<String>> LAST_ACTIVE_DISKS = new java.util.HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Only run on server side
        if (player.level().isClientSide) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);
        Set<String> currentDisks = new HashSet<>();

        // Apply effects from all equipped disks
        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null) {
                currentDisks.add(diskId);
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk != null) {
                    int level = diskData.getDiskLevel(diskId);
                    disk.applyEffect(player, level);
                }
            }
        }

        // Remove effects from disks that were unequipped
        Set<String> lastDisks = LAST_ACTIVE_DISKS.getOrDefault(player.getUUID(), new HashSet<>());
        for (String diskId : lastDisks) {
            if (!currentDisks.contains(diskId)) {
                // Disk was unequipped, remove its effects
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk != null) {
                    disk.removeEffect(player);
                }
            }
        }

        // Update tracking
        LAST_ACTIVE_DISKS.put(player.getUUID(), currentDisks);
    }
}