package com.nedraw.upgrading.network.packet;

import com.nedraw.upgrading.ServerEvents;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRarity;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import com.nedraw.upgrading.item.ModItems;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
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

                // Get all disks of this rarity
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

                // Pick a random disk from this rarity
                UpgradeDisk chosenDisk = allRarityDisks.get(new Random().nextInt(allRarityDisks.size()));

                // Check if player already has this disk unlocked
                boolean alreadyUnlocked = diskData.isDiskUnlocked(chosenDisk.getId());

                if (alreadyUnlocked) {
                    // DUPLICATE! Give them the physical disk item instead
                    ItemStack diskItem = getDiskItemStack(chosenDisk.getId());

                    if (diskItem != null) {
                        // Add to inventory or drop if full
                        if (!player.addItem(diskItem)) {
                            player.drop(diskItem, false);
                        }

                        player.displayClientMessage(
                                Component.literal("Duplicate! Received ")
                                        .append(Component.literal(chosenDisk.getDisplayName())
                                                .withStyle(style -> style.withColor(chosenDisk.getRarity().getColor())))
                                        .append(Component.literal(" as an item!"))
                                        .withStyle(style -> style.withColor(0xFFAA00)),
                                true
                        );
                    }
                } else {
                    // NEW DISK! Unlock it
                    diskData.unlockDisk(chosenDisk.getId());

                    player.displayClientMessage(
                            Component.translatable("message.upgrading.disk_unlocked", chosenDisk.getDisplayName())
                                    .withStyle(style -> style.withColor(0x55FF55)),
                            true
                    );
                }

                // Consume the encrypted floppy from player's hand
                for (net.minecraft.world.InteractionHand hand : net.minecraft.world.InteractionHand.values()) {
                    net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
                    if (stack.getItem() instanceof com.nedraw.upgrading.item.EncryptedFloppyItem) {
                        stack.shrink(1);
                        break;
                    }
                }

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

    // Helper method to get the physical disk item for a disk ID
    private static ItemStack getDiskItemStack(String diskId) {
        return switch (diskId) {
            case "swift_feet" -> new ItemStack(ModItems.SWIFT_FEET_DISK.get());
            case "sea_fish" -> new ItemStack(ModItems.SEA_FISH_DISK.get());
            case "magnet" -> new ItemStack(ModItems.MAGNET_DISK.get());
            case "mighty_miner" -> new ItemStack(ModItems.MIGHTY_MINER_DISK.get());
            case "night_vision" -> new ItemStack(ModItems.NIGHT_VISION_DISK.get());
            case "lightweight" -> new ItemStack(ModItems.LIGHTWEIGHT_DISK.get());
            case "feather_fall" -> new ItemStack(ModItems.FEATHER_FALL_DISK.get());
            case "efficient" -> new ItemStack(ModItems.EFFICIENT_DISK.get());
            case "tanky" -> new ItemStack(ModItems.TANKY_DISK.get());
            case "flame_walker" -> new ItemStack(ModItems.FLAME_WALKER_DISK.get());
            case "step_assist" -> new ItemStack(ModItems.STEP_ASSIST_DISK.get());
            case "harvester" -> new ItemStack(ModItems.HARVESTER_DISK.get());
            case "glutton" -> new ItemStack(ModItems.GLUTTON_DISK.get());
            case "soapy_hands" -> new ItemStack(ModItems.SOAPY_HANDS_DISK.get());
            case "berserker" -> new ItemStack(ModItems.BERSERKER_DISK.get());
            case "pyroclasm" -> new ItemStack(ModItems.PYROCLASM_DISK.get());
            default -> null;
        };
    }
}