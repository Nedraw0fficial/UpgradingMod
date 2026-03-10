package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.IronGripDisk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class IronGripHandler {

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("iron_grip")) {
                var disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof IronGripDisk ironGripDisk) {
                    int level = diskData.getDiskLevel(diskId);
                    float reduction = ironGripDisk.getKnockbackReduction(level);

                    // Level 12: Cancel knockback if sneaking near edge
                    if (level >= 12 && player.isCrouching()) {
                        if (isNearEdge(player)) {
                            event.setCanceled(true);
                            return;
                        }
                    }

                    // Reduce knockback strength
                    float originalStrength = event.getOriginalStrength();
                    float newStrength = originalStrength * (1.0f - reduction);
                    event.setStrength(newStrength);
                }

                return;
            }
        }
    }

    private static boolean isNearEdge(Player player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        // Check if there's no solid block below in a 1-block radius
        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                BlockPos checkPos = playerPos.offset(xOffset, -1, zOffset);
                if (!level.getBlockState(checkPos).isSolid()) {
                    return true; // Found an edge (no solid block below)
                }
            }
        }

        return false; // All blocks below are solid
    }
}