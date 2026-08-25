package com.nedraw.upgrading.client;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.client.screen.DiskMenuScreen;
import com.nedraw.upgrading.network.packet.ActivateMythicPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = UpgradingMod.MODID, value = Dist.CLIENT)
public class InputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.screen != null) return;

        if (ModKeyBinds.OPEN_DISK_MENU.consumeClick()) {
            mc.setScreen(new DiskMenuScreen());
        }

        if (ModKeyBinds.ACTIVATE_SLOT_1.consumeClick()) {
            PacketDistributor.sendToServer(new ActivateMythicPacket(0));
        }
        if (ModKeyBinds.ACTIVATE_SLOT_2.consumeClick()) {
            PacketDistributor.sendToServer(new ActivateMythicPacket(1));
        }
        if (ModKeyBinds.ACTIVATE_SLOT_3.consumeClick()) {
            PacketDistributor.sendToServer(new ActivateMythicPacket(2));
        }
    }
}