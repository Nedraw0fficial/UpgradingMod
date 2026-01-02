package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.MightyMinerDisk;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class BlockBreakHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();

        // Server-side only
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        // Check all equipped disks
        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);

            if ("mighty_miner".equals(diskId)) {
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof MightyMinerDisk mightyMiner) {
                    int level = diskData.getDiskLevel(diskId);

                    // Handle ore finding at level 12
                    mightyMiner.handleBlockBreak(
                            player,
                            event.getState(),
                            event.getPos(),
                            level
                    );
                }
            } else if ("harvester".equals(diskId)) {
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof com.nedraw.upgrading.disk.HarvesterDisk harvester) {
                    int level = diskData.getDiskLevel(diskId);

                    // Handle crop duplication
                    harvester.handleCropBreak(
                            player,
                            event.getState(),
                            event.getPos(),
                            level
                    );
                }
            }
        }
    }
}