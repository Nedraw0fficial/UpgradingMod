package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.ArchitectsMindDisk;
import com.nedraw.upgrading.disk.DiskRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class ArchitectsMindHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        PlayerDiskData data = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = data.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("architects_mind")) {
                int level = data.getDiskLevel(diskId);
                if (level >= 12) {
                    player.stopUsingItem();
                }
                return;
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        PlayerDiskData data = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = data.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("architects_mind")) {
                var disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof ArchitectsMindDisk) {
                    // Get player's current max reach
                    var reachAttr = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
                    if (reachAttr == null) return;

                    double maxReach = reachAttr.getValue();

                    // Calculate distance from player eye to placed block center
                    BlockPos pos = event.getPos();
                    double distance = player.getEyePosition().distanceTo(
                            pos.getCenter()
                    );

                    // Fire if placing near the maximum reach (within 1 block of max)
                    if (distance >= maxReach - 1.0) {
                        ModAdvancementTriggers.MAX_REACH_PLACEMENT(player);
                    }
                }
                return;
            }
        }
    }
}