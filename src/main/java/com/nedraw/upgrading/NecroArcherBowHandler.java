package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRarity;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class NecroArcherBowHandler {

    @SubscribeEvent
    public static void onArrowLoose(ArrowLooseEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId == null) continue;

            UpgradeDisk disk = DiskRegistry.getDisk(diskId);
            if (disk == null) continue;
            if (disk.getRarity() != DiskRarity.MYTHIC) continue;
            if (!diskId.equals("necro_archer")) continue;

            int level = diskData.getDiskLevel(diskId);
            boolean inBoost = NecroArcherHandler.isPlayerBoosted(player.getUUID());

            // Charge here is the raw tick count the bow was drawn (0-20+ ticks)
            int originalCharge = event.getCharge();

            int boostedCharge;
            if (inBoost && level >= 12) {
                // L12 boost: zero draw time = always full power (20 ticks = max)
                boostedCharge = 20;
            } else if (inBoost) {
                // L11 boost: 80% faster draw = reach full charge much sooner
                boostedCharge = Math.round(originalCharge * 1.8F);
            } else if (level >= 12) {
                // Passive L12: 80% faster
                boostedCharge = Math.round(originalCharge * 1.8F);
            } else {
                // Passive L11: 60% faster
                boostedCharge = Math.round(originalCharge * 1.6F);
            }

            event.setCharge(boostedCharge);
            return;
        }
    }
}