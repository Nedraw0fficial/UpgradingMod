package com.nedraw.upgrading.network.packet;

import com.nedraw.upgrading.ServerEvents;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpgradeDiskPacket(String diskId) implements CustomPacketPayload {

    public static final Type<UpgradeDiskPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "upgrade_disk"));

    public static final StreamCodec<ByteBuf, UpgradeDiskPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            UpgradeDiskPacket::diskId,
            UpgradeDiskPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpgradeDiskPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound() && context.player() instanceof ServerPlayer serverPlayer) {
                PlayerDiskData data = PlayerDiskData.get(serverPlayer);
                UpgradeDisk disk = DiskRegistry.getDisk(packet.diskId());

                if (disk == null || !data.isDiskUnlocked(packet.diskId())) {
                    return;
                }

                int currentLevel = data.getDiskLevel(packet.diskId());

                if (!disk.canUpgrade(currentLevel)) {
                    return;
                }

                int xpCost = disk.getRarity().getXpCostForLevel(currentLevel);

                // Check if player has enough XP
                if (!data.hasEnoughXP(serverPlayer, xpCost)) {
                    serverPlayer.displayClientMessage(
                            Component.literal("Not enough XP!").withStyle(style -> style.withColor(0xFF5555)),
                            true
                    );
                    return;
                }

                // Consume XP and upgrade
                data.consumeXP(serverPlayer, xpCost);
                data.upgradeDisk(packet.diskId());

                // Success feedback
                serverPlayer.displayClientMessage(
                        Component.literal("Upgraded ")
                                .append(Component.literal(disk.getDisplayName())
                                        .withStyle(style -> style.withColor(disk.getRarity().getColor())))
                                .append(Component.literal(" to Level " + (currentLevel + 1)))
                                .withStyle(style -> style.withColor(0x55FF55)),
                        true
                );

                // Play level-up sound
                serverPlayer.level().playSound(
                        serverPlayer,
                        serverPlayer.blockPosition(),
                        SoundEvents.PLAYER_LEVELUP,
                        SoundSource.PLAYERS,
                        1.0f,
                        1.5f
                );

                // Sync back to client
                ServerEvents.syncDiskData(serverPlayer);
            }
        });
    }
}