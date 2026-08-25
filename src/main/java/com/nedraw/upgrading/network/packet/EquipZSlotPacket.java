package com.nedraw.upgrading.network.packet;

import com.nedraw.upgrading.ServerEvents;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.item.ZSlotItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record EquipZSlotPacket(int slot, boolean equip) implements CustomPacketPayload {

    public static final Type<EquipZSlotPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "equip_z_slot"));

    public static final StreamCodec<ByteBuf, EquipZSlotPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, EquipZSlotPacket::slot,
            ByteBufCodecs.BOOL, EquipZSlotPacket::equip,
            EquipZSlotPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(EquipZSlotPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!context.flow().isServerbound()) return;
            if (!(context.player() instanceof ServerPlayer player)) return;

            PlayerDiskData data = PlayerDiskData.get(player);
            int slot = packet.slot();
            if (slot < 0 || slot >= 3) return;

            if (packet.equip()) {
                // Take Z-Slot from main hand
                ItemStack held = player.getMainHandItem();
                if (held.isEmpty() || !(held.getItem() instanceof ZSlotItem)) return;

                // Give back existing Z-Slot if any
                ItemStack existing = data.getZSlot(slot);
                if (!existing.isEmpty()) {
                    if (!player.addItem(existing.copy())) {
                        player.drop(existing.copy(), false);
                    }
                }

                // Equip one Z-Slot from hand
                ItemStack toEquip = held.copyWithCount(1);
                held.shrink(1);
                data.setZSlot(slot, toEquip);
            } else {
                // Unequip Z-Slot back to inventory
                ItemStack zSlot = data.getZSlot(slot);
                if (zSlot.isEmpty()) return;

                if (!player.addItem(zSlot.copy())) {
                    player.drop(zSlot.copy(), false);
                }
                data.setZSlot(slot, ItemStack.EMPTY);
            }

            ServerEvents.syncDiskData(player);
        });
    }
}