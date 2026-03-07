package com.nedraw.upgrading;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.FeatherFallDisk;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class FeatherFallHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Check if it's fall damage
        if (!event.getSource().is(DamageTypes.FALL)) return;

        // Check if player has Feather Fall equipped
        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("feather_fall")) {
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof FeatherFallDisk featherFallDisk) {
                    int level = diskData.getDiskLevel(diskId);

                    // Reduce damage
                    float originalDamage = event.getOriginalDamage();
                    float newDamage = featherFallDisk.reduceFallDamage(originalDamage, level);

                    event.setNewDamage(newDamage);

                    return; // Only apply one Feather Fall disk
                }
            }
        }
    }
}