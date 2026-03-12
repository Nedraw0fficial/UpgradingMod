package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class ArchitectsMindHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        // Check if player has Architect's Mind equipped at L12
        PlayerDiskData data = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = data.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("architects_mind")) {
                int level = data.getDiskLevel(diskId);

                if (level >= 12) {
                    // INSTANT PLACEMENT
                    // In Minecraft, there's a field that tracks item use duration
                    // We need to reset it to 0 every tick
                    // The field is: useItemRemaining in LivingEntity

                    // This forces the player to be able to use items immediately
                    player.stopUsingItem();
                }

                return;
            }
        }
    }
}