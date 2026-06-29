package com.nedraw.upgrading.network.packet;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.DiskRarity;
import com.nedraw.upgrading.disk.UpgradeDisk;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ActivateMythicPacket() implements CustomPacketPayload {

    public static final Type<ActivateMythicPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "activate_mythic"));

    public static final StreamCodec<ByteBuf, ActivateMythicPacket> STREAM_CODEC =
            StreamCodec.unit(new ActivateMythicPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ActivateMythicPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound() && context.player() instanceof ServerPlayer serverPlayer) {
                PlayerDiskData data = PlayerDiskData.get(serverPlayer);

                // Check left slot (slot 0) for a MYTHIC disk
                String diskId = data.getEquippedDisk(0);
                if (diskId == null) return;

                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk == null) return;

                // Only works for MYTHIC disks
                if (disk.getRarity() != DiskRarity.MYTHIC) return;

                // Check cooldown
                long currentTime = System.currentTimeMillis();
                long lastActivation = data.getAbilityCooldown(diskId);
                long cooldownMs = disk.getAbilityCooldownMs(data.getDiskLevel(diskId));

                if (currentTime - lastActivation < cooldownMs) {
                    // Still on cooldown - optionally notify player
                    long remainingSeconds = (cooldownMs - (currentTime - lastActivation)) / 1000;
                    serverPlayer.displayClientMessage(
                            net.minecraft.network.chat.Component.literal(
                                            disk.getDisplayName() + " ability on cooldown! (" + remainingSeconds + "s)")
                                    .withStyle(style -> style.withColor(0xFF5555)),
                            true
                    );
                    return;
                }

                // Set cooldown BEFORE activating (prevent spamming)
                data.setAbilityCooldown(diskId, currentTime);

                // Trigger the ability
                disk.activateAbility(serverPlayer, data.getDiskLevel(diskId));
            }
        });
    }
}