package com.nedraw.upgrading.client;

import com.nedraw.upgrading.UpgradingMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerKeyBinds(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBinds.OPEN_DISK_MENU);
        event.register(ModKeyBinds.ACTIVATE_MYTHIC);
    }
}