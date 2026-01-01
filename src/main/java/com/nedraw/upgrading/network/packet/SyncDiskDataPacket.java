package com.nedraw.upgrading.network.packet;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record SyncDiskDataPacket(
        Set<String> unlockedDisks,
        Map<String, Integer> diskLevels,  // NEW: Include levels
        String slot0,
        String slot1,
        String slot2
) implements CustomPacketPayload {

    public static final Type<SyncDiskDataPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "sync_disk_data"));

    public static final StreamCodec<ByteBuf, SyncDiskDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8),
            SyncDiskDataPacket::unlockedDisks,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT),
            SyncDiskDataPacket::diskLevels,
            ByteBufCodecs.STRING_UTF8,
            SyncDiskDataPacket::slot0,
            ByteBufCodecs.STRING_UTF8,
            SyncDiskDataPacket::slot1,
            ByteBufCodecs.STRING_UTF8,
            SyncDiskDataPacket::slot2,
            SyncDiskDataPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncDiskDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isClientbound()) {
                var player = context.player();
                if (player != null) {
                    PlayerDiskData data = PlayerDiskData.get(player);

                    // Sync unlocked disks
                    for (String diskId : packet.unlockedDisks()) {
                        data.unlockDisk(diskId);
                    }

                    // Sync disk levels
                    for (Map.Entry<String, Integer> entry : packet.diskLevels().entrySet()) {
                        data.setDiskLevel(entry.getKey(), entry.getValue());
                    }

                    // Sync equipped slots
                    data.unequipSlot(0);
                    data.unequipSlot(1);
                    data.unequipSlot(2);

                    if (!packet.slot0().isEmpty()) data.equipDisk(packet.slot0(), 0);
                    if (!packet.slot1().isEmpty()) data.equipDisk(packet.slot1(), 1);
                    if (!packet.slot2().isEmpty()) data.equipDisk(packet.slot2(), 2);
                }
            }
        });
    }
}