package com.nedraw.upgrading.network.packet;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record SyncDiskDataPacket(
        Set<String> unlockedDisks,
        Map<String, Integer> diskLevels,
        String slot0,
        String slot1,
        String slot2,
        ItemStack zSlot0,
        ItemStack zSlot1,
        ItemStack zSlot2
) implements CustomPacketPayload {

    public static final Type<SyncDiskDataPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "sync_disk_data"));

    // Helper record to split the 8 fields into two groups of 4
    private record Part1(Set<String> unlockedDisks, Map<String, Integer> diskLevels, String slot0, String slot1) {}
    private record Part2(String slot2, ItemStack zSlot0, ItemStack zSlot1, ItemStack zSlot2) {}

    private static final StreamCodec<RegistryFriendlyByteBuf, Part1> PART1_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), Part1::unlockedDisks,
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.INT), Part1::diskLevels,
                    ByteBufCodecs.STRING_UTF8, Part1::slot0,
                    ByteBufCodecs.STRING_UTF8, Part1::slot1,
                    Part1::new
            );

    private static final StreamCodec<RegistryFriendlyByteBuf, Part2> PART2_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, Part2::slot2,
                    ItemStack.OPTIONAL_STREAM_CODEC, Part2::zSlot0,
                    ItemStack.OPTIONAL_STREAM_CODEC, Part2::zSlot1,
                    ItemStack.OPTIONAL_STREAM_CODEC, Part2::zSlot2,
                    Part2::new
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncDiskDataPacket> STREAM_CODEC =
            StreamCodec.composite(
                    PART1_CODEC, p -> new Part1(p.unlockedDisks(), p.diskLevels(), p.slot0(), p.slot1()),
                    PART2_CODEC, p -> new Part2(p.slot2(), p.zSlot0(), p.zSlot1(), p.zSlot2()),
                    (p1, p2) -> new SyncDiskDataPacket(
                            p1.unlockedDisks(), p1.diskLevels(), p1.slot0(), p1.slot1(),
                            p2.slot2(), p2.zSlot0(), p2.zSlot1(), p2.zSlot2()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncDiskDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().isClientbound()) return;
            var player = context.player();
            if (player == null) return;

            PlayerDiskData data = PlayerDiskData.get(player);

            for (String diskId : packet.unlockedDisks()) {
                data.unlockDisk(diskId);
            }
            for (Map.Entry<String, Integer> entry : packet.diskLevels().entrySet()) {
                data.setDiskLevel(entry.getKey(), entry.getValue());
            }

            data.unequipSlot(0);
            data.unequipSlot(1);
            data.unequipSlot(2);

            if (!packet.slot0().isEmpty()) data.equipDisk(packet.slot0(), 0);
            if (!packet.slot1().isEmpty()) data.equipDisk(packet.slot1(), 1);
            if (!packet.slot2().isEmpty()) data.equipDisk(packet.slot2(), 2);

            data.setZSlot(0, packet.zSlot0());
            data.setZSlot(1, packet.zSlot1());
            data.setZSlot(2, packet.zSlot2());
        });
    }
}