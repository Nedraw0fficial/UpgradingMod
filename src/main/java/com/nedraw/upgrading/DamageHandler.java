package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.FlameWalkerDisk;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class DamageHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Pre event) {
        // Only handle player damage
        if (!(event.getEntity() instanceof Player player)) return;

        // Check if damage is fire-related
        var damageSource = event.getSource();
        if (damageSource.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            PlayerDiskData diskData = PlayerDiskData.get(player);

            // Check all equipped disks for Flame Walker
            for (int slot = 0; slot < 3; slot++) {
                String diskId = diskData.getEquippedDisk(slot);
                if ("flame_walker".equals(diskId)) {
                    UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                    if (disk instanceof FlameWalkerDisk flameWalker) {
                        int level = diskData.getDiskLevel(diskId);

                        // Reduce fire damage
                        float originalDamage = event.getOriginalDamage();
                        float reducedDamage = flameWalker.reduceFireDamage(originalDamage, level);

                        // Set new damage amount
                        event.setNewDamage(reducedDamage);
                    }
                    break; // Only one Flame Walker can be equipped
                }
            }
        }
    }
}