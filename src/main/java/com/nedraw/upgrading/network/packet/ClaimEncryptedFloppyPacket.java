package com.nedraw.upgrading.network.packet;

import com.nedraw.upgrading.ServerEvents;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRarity;
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

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public record ClaimEncryptedFloppyPacket(String rarityName) implements CustomPacketPayload {

    public static final Type<ClaimEncryptedFloppyPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "claim_encrypted_floppy"));

    public static final StreamCodec<ByteBuf, ClaimEncryptedFloppyPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ClaimEncryptedFloppyPacket::rarityName,
            ClaimEncryptedFloppyPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClaimEncryptedFloppyPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound() && context.player() instanceof ServerPlayer player) {

                // Parse rarity
                DiskRarity rarity;
                try {
                    rarity = DiskRarity.valueOf(packet.rarityName());
                } catch (IllegalArgumentException e) {
                    return;
                }

                PlayerDiskData diskData = PlayerDiskData.get(player);

                // Get all disks of this rarity that player doesn't have
                List<UpgradeDisk> availableDisks = DiskRegistry.getAllDisks().stream()
                        .filter(disk -> disk.getRarity() == rarity)
                        .filter(disk -> !diskData.isDiskUnlocked(disk.getId()))
                        .collect(Collectors.toList());

                UpgradeDisk chosenDisk;

                if (availableDisks.isEmpty()) {
                    // Player has all disks of this rarity!
                    // Give them a random disk from this rarity anyway (for duplicate levels)
                    List<UpgradeDisk> allRarityDisks = DiskRegistry.getAllDisks().stream()
                            .filter(disk -> disk.getRarity() == rarity)
                            .collect(Collectors.toList());

                    if (allRarityDisks.isEmpty()) {
                        // No disks of this rarity exist (shouldn't happen)
                        player.displayClientMessage(
                                Component.translatable("message.upgrading.encrypted_floppy.error"),
                                false
                        );
                        return;
                    }

                    chosenDisk = allRarityDisks.get(new Random().nextInt(allRarityDisks.size()));
                } else {
                    // Pick random disk from available
                    chosenDisk = availableDisks.get(new Random().nextInt(availableDisks.size()));
                }

                // Unlock the disk
                diskData.unlockDisk(chosenDisk.getId());

                // Consume the encrypted floppy from player's hand
                boolean consumed = false;
                for (net.minecraft.world.InteractionHand hand : net.minecraft.world.InteractionHand.values()) {
                    net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
                    if (stack.getItem() instanceof com.nedraw.upgrading.item.EncryptedFloppyItem) {
                        stack.shrink(1);
                        consumed = true;
                        break;
                    }
                }

                // Simple unlock message (same as disk item unlock)
                player.displayClientMessage(
                        Component.translatable("message.upgrading.disk_unlocked", chosenDisk.getDisplayName())
                                .withStyle(style -> style.withColor(0x55FF55)),
                        true
                );

                // Spawn firework particles
                for (int i = 0; i < 50; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double radius = Math.random() * 3.0;
                    double px = player.getX() + Math.cos(angle) * radius;
                    double py = player.getY() + 1.0 + Math.random() * 2.0;
                    double pz = player.getZ() + Math.sin(angle) * radius;

                    ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                            net.minecraft.core.particles.ParticleTypes.FIREWORK,
                            px, py, pz,
                            1,
                            0, 0.5, 0,
                            0.1
                    );
                }

                // Play epic sound
                player.level().playSound(
                        null,
                        player.blockPosition(),
                        SoundEvents.PLAYER_LEVELUP,
                        SoundSource.PLAYERS,
                        1.0f,
                        1.0f
                );

                // Sync to client
                ServerEvents.syncDiskData(player);
            }
        });
    }
}