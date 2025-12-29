package com.nedraw.upgrading.client;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.client.screen.DiskMenuScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID, value = Dist.CLIENT)
public class InputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.screen != null) {
            return; // Don't open if player doesn't exist or another screen is open
        }

        if (ModKeyBinds.OPEN_DISK_MENU.consumeClick()) {
            mc.setScreen(new DiskMenuScreen());
        }
    }
}