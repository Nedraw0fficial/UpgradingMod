package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.network.packet.SyncDiskDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class ServerEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncDiskData(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncDiskData(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncDiskData(serverPlayer);
        }
    }

    public static void syncDiskData(ServerPlayer player) {
        PlayerDiskData data = PlayerDiskData.get(player);

        String slot0 = data.getEquippedDisk(0);
        String slot1 = data.getEquippedDisk(1);
        String slot2 = data.getEquippedDisk(2);

        // Build disk levels map
        Map<String, Integer> diskLevels = new HashMap<>();
        for (String diskId : data.getUnlockedDisks()) {
            diskLevels.put(diskId, data.getDiskLevel(diskId));
        }

        SyncDiskDataPacket packet = new SyncDiskDataPacket(
                new HashSet<>(data.getUnlockedDisks()),
                diskLevels,  // Include levels now
                slot0 != null ? slot0 : "",
                slot1 != null ? slot1 : "",
                slot2 != null ? slot2 : ""
        );

        PacketDistributor.sendToPlayer(player, packet);
    }
}