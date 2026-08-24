package com.nedraw.upgrading.network.packet;

import com.nedraw.upgrading.ServerEvents;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.DiskRarity;
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

    public static int getFragmentCost(DiskRarity rarity) {
        return switch (rarity) {
            case BASIC     -> 1;
            case RARE      -> 3;
            case EPIC      -> 7;
            case LEGENDARY -> 15;
            case MYTHIC    -> 32;
        };
    }

    private static int countFragments(ServerPlayer player) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.ENCRYPTED_FRAGMENT.get())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void consumeFragments(ServerPlayer player, int amount) {
        int remaining = amount;
        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) break;
            if (stack.is(ModItems.ENCRYPTED_FRAGMENT.get())) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.shrink(toRemove);
                remaining -= toRemove;
            }
        }
    }

    public static void handle(UpgradeDiskPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.flow().isServerbound() && context.player() instanceof ServerPlayer serverPlayer) {
                PlayerDiskData data = PlayerDiskData.get(serverPlayer);
                UpgradeDisk disk = DiskRegistry.getDisk(packet.diskId());

                if (disk == null || !data.isDiskUnlocked(packet.diskId())) return;

                int currentLevel = data.getDiskLevel(packet.diskId());

                if (!disk.canUpgrade(currentLevel)) return;

                int xpCost = disk.getRarity().getXpCostForLevel(currentLevel);

                if (!data.hasEnoughXP(serverPlayer, xpCost)) {
                    serverPlayer.displayClientMessage(
                            Component.translatable("message.upgrading.not_enough_xp")
                                    .withStyle(s -> s.withColor(0xFF5555)), true);
                    return;
                }

                System.out.println("!!!! UpgradeDiskPacket received! Level: " + currentLevel + " needsFragments: " + (currentLevel == 11));

                // L11 → L12 requires fragments
                boolean needsFragments = currentLevel == 11;
                if (needsFragments) {
                    int fragmentCost = getFragmentCost(disk.getRarity());
                    int found = countFragments(serverPlayer);
                    System.out.println("!!!! Fragment cost: " + fragmentCost + " Found: " + found);
                    if (found < fragmentCost) {
                        serverPlayer.displayClientMessage(
                                Component.translatable("message.upgrading.not_enough_fragments")
                                        .withStyle(s -> s.withColor(0xFF5555)), true);
                        return;
                    }
                    consumeFragments(serverPlayer, fragmentCost);
                }

                data.consumeXP(serverPlayer, xpCost);
                data.upgradeDisk(packet.diskId());

                serverPlayer.displayClientMessage(
                        Component.literal("Upgraded ")
                                .append(Component.literal(disk.getDisplayName())
                                        .withStyle(s -> s.withColor(disk.getRarity().getColor())))
                                .append(Component.literal(" to Level " + (currentLevel + 1)))
                                .withStyle(s -> s.withColor(0x55FF55)), true);

                serverPlayer.level().playSound(serverPlayer, serverPlayer.blockPosition(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.5f);

                ServerEvents.syncDiskData(serverPlayer);
            }
        });
    }
}