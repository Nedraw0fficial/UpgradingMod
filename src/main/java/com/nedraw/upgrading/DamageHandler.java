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
        if (!(event.getEntity() instanceof Player player)) return;

        var damageSource = event.getSource();
        if (damageSource.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            PlayerDiskData diskData = PlayerDiskData.get(player);

            for (int slot = 0; slot < 3; slot++) {
                String diskId = diskData.getEquippedDisk(slot);
                if ("flame_walker".equals(diskId)) {
                    UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                    if (disk instanceof FlameWalkerDisk flameWalker) {
                        int level = diskData.getDiskLevel(diskId);
                        float efficiency = ZSlotEffects.getEfficiencyMultiplier(player, slot);
                        float reducedDamage = flameWalker.reduceFireDamage(event.getOriginalDamage(), level, efficiency);
                        event.setNewDamage(reducedDamage);
                    }
                    break;
                }
            }
        }
    }
}
