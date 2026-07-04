package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.BerserkerDisk;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class BerserkerHandler {

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        // The killer must be a player
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        // Check if player is below 30% HP
        float healthPercent = (player.getHealth() / player.getMaxHealth()) * 100;
        if (healthPercent >= 30) return;

        // Check if player has Berserker at L12 equipped (rage bonus only active at L12)
        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("berserker")) {
                var disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof BerserkerDisk) {
                    int level = diskData.getDiskLevel(diskId);

                    // Rage bonus only active at L12 below 30% HP
                    if (level >= 12) {
                        ModAdvancementTriggers.BERSERKER_RAGE_KILL(player);
                    }
                }
                return;
            }
        }
    }
}