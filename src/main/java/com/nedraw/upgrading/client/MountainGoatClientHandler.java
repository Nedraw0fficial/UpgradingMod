package com.nedraw.upgrading.client;

import com.nedraw.upgrading.MountainGoatHandler;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.network.packet.WallJumpPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

@EventBusSubscriber(modid = UpgradingMod.MODID, value = Dist.CLIENT)
public class MountainGoatClientHandler {

    private static long lastJump = 0;

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (event.getKey() == GLFW.GLFW_KEY_SPACE && event.getAction() == GLFW.GLFW_PRESS) {
            long now = System.currentTimeMillis();

            // Prevent spam
            if (now - lastJump < 300) return;

            UUID id = mc.player.getUUID();

            // Check if clinging via handler state
            if (MountainGoatHandler.CLING_START.containsKey(id)) {
                Direction wall = MountainGoatHandler.CLING_WALL.get(id);

                if (wall != null) {
                    // Check level
                    PlayerDiskData data = PlayerDiskData.get(mc.player);
                    int level = data.getDiskLevel("mountain_goat");

                    if (level >= 12) {
                        PacketDistributor.sendToServer(new WallJumpPacket(wall.get3DDataValue()));
                        lastJump = now;

                        //mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal("→ JUMP SENT!").withStyle(style -> style.withColor(0x00FFFF)), true);
                    }
                }
            }
        }
    }
}