package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.IronGripDisk;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
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

                            // Fire advancement - player resisted being knocked off an edge!
                            if (player instanceof ServerPlayer sp) {
                                ModAdvancementTriggers.EDGE_KNOCKBACK_RESISTED(sp);
                            }
                            return;
                        }
                    }

                    // Reduce knockback strength
                    float newStrength = event.getOriginalStrength() * (1.0f - reduction);
                    event.setStrength(newStrength);
                }
                return;
            }
        }
    }

    private static boolean isNearEdge(Player player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        for (int xOffset = -1; xOffset <= 1; xOffset++) {
            for (int zOffset = -1; zOffset <= 1; zOffset++) {
                BlockPos checkPos = playerPos.offset(xOffset, -1, zOffset);
                if (!level.getBlockState(checkPos).isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }
}