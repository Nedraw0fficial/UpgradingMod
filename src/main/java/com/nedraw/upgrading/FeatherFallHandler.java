package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.FeatherFallDisk;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.server.level.ServerPlayer;
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
        if (!event.getSource().is(DamageTypes.FALL)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("feather_fall")) {
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof FeatherFallDisk featherFallDisk) {
                    int level = diskData.getDiskLevel(diskId);

                    float originalDamage = event.getOriginalDamage();
                    float newDamage = featherFallDisk.reduceFallDamage(originalDamage, level);

                    event.setNewDamage(newDamage);

                    // Fire advancement if the fall would have been lethal without the disk
                    // and the disk saved us (newDamage < player.getHealth() but originalDamage >= it)
                    if (originalDamage >= player.getHealth() && newDamage < player.getHealth()
                            && player instanceof ServerPlayer sp) {
                        ModAdvancementTriggers.LETHAL_FALL_SURVIVED(sp);
                    }

                    return;
                }
            }
        }
    }
}