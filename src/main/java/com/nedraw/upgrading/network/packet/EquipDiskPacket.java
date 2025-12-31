package com.nedraw.upgrading.network.packet;

import com.nedraw.upgrading.ServerEvents;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EquipDiskPacket(String diskId, int slot, boolean unequip) implements CustomPacketPayload {

    public static final Type<EquipDiskPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "equip_disk"));

    public static final StreamCodec<ByteBuf, EquipDiskPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            EquipDiskPacket::diskId,
            ByteBufCodecs.INT,
            EquipDiskPacket::slot,
            ByteBufCodecs.BOOL,
            EquipDiskPacket::unequip,
            EquipDiskPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(EquipDiskPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound() && context.player() instanceof ServerPlayer serverPlayer) {
                PlayerDiskData data = PlayerDiskData.get(serverPlayer);

                if (packet.unequip()) {
                    data.unequipSlot(packet.slot());
                } else {
                    data.equipDisk(packet.diskId(), packet.slot());
                }

                // Play sound
                serverPlayer.level().playSound(
                        null,
                        serverPlayer.blockPosition(),
                        packet.unequip() ? SoundEvents.ITEM_PICKUP : SoundEvents.ARMOR_EQUIP_GENERIC.value(),
                        SoundSource.PLAYERS,
                        0.8f,
                        packet.unequip() ? 0.8f : 1.2f
                );

                // Sync back to client
                ServerEvents.syncDiskData(serverPlayer);
            }
        });
    }
}