package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.*;
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
        if (!(player.level() instanceof ServerLevel)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            float efficiency = ZSlotEffects.getEfficiencyMultiplier(player, slot);

            if ("mighty_miner".equals(diskId)) {
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof MightyMinerDisk mightyMiner) {
                    mightyMiner.handleBlockBreak(player, event.getState(), event.getPos(),
                            diskData.getDiskLevel(diskId), efficiency);
                }
            } else if ("harvester".equals(diskId)) {
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof HarvesterDisk harvester) {
                    harvester.handleCropBreak(player, event.getState(), event.getPos(),
                            diskData.getDiskLevel(diskId), efficiency);
                }
            } else if ("block_o_enchanting".equals(diskId)) {
                var disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof BlockOEnchantingDisk blockOEnchanting) {
                    boolean dropsXP = event.getState().getExpDrop(
                            event.getLevel(),
                            event.getPos(),
                            null,
                            player,
                            player.getMainHandItem()
                    ) > 0;
                    blockOEnchanting.handleBlockBreak(player,
                            dropsXP,
                            diskData.getDiskLevel(diskId),
                            efficiency);
                }
            }
        }
    }
}
