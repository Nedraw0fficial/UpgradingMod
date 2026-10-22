package com.nedraw.upgrading.client;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.network.packet.WallJumpPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = UpgradingMod.MODID, value = Dist.CLIENT)
public class MountainGoatClientHandler {

    private static long lastJump = 0;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (event.getKey() == GLFW.GLFW_KEY_SPACE && event.getAction() == GLFW.GLFW_PRESS) {
            long now = System.currentTimeMillis();
            if (now - lastJump < 300) return;
            if (mc.player.onGround()) return;

            PlayerDiskData data = PlayerDiskData.get(mc.player);
            boolean equipped = false;
            for (int slot = 0; slot < 3; slot++) {
                if ("mountain_goat".equals(data.getEquippedDisk(slot))) {
                    equipped = true;
                    break;
                }
            }
            if (!equipped) return;
            if (data.getDiskLevel("mountain_goat") < 12) return;

            PacketDistributor.sendToServer(new WallJumpPacket(0));
            lastJump = now;
        }
    }
}