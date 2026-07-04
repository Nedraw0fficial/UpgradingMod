package com.nedraw.upgrading.client;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.effect.ModEffects;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerHeartTypeEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID, value = Dist.CLIENT)
public class NecromisisHeartHandler {

    @SubscribeEvent
    public static void onHeartType(PlayerHeartTypeEvent event) {
        if (event.getEntity().hasEffect(ModEffects.NECROMISIS)) {
            event.setType(NecromisisHeartType.get());
        }
    }
}