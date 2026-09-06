package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class PhantomHandler {

    @SubscribeEvent
    public static void onTargetChange(LivingChangeTargetEvent event) {
        if (!(event.getNewAboutToBeSetTarget() instanceof Player player)) return;
        if (!player.hasEffect(MobEffects.INVISIBILITY)) return;

        PlayerDiskData data = PlayerDiskData.get(player);
        for (int slot = 0; slot < 3; slot++) {
            String diskId = data.getEquippedDisk(slot);
            if ("phantom".equals(diskId) && data.getDiskLevel(diskId) >= 12) {
                event.setCanceled(true);
                return;
            }
        }
    }
}