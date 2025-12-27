package com.nedraw.upgrading.item;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

public class DiskItem extends Item {
    private final String diskId;

    public DiskItem(String diskId) {
        super(new Properties()
                .stacksTo(1) // Disks don't stack
                .rarity(getRarityForDisk(diskId))
        );
        this.diskId = diskId;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Get player's disk data
            PlayerDiskData diskData = PlayerDiskData.get(player);

            // Check if already unlocked
            if (diskData.isDiskUnlocked(diskId)) {
                // Show error message
                player.displayClientMessage(
                        Component.literal("You already have this disk unlocked!")
                                .withStyle(style -> style.withColor(0xFF5555)), // Red
                        true // Show in hotbar
                );

                // Play error sound
                level.playSound(null, player.blockPosition(),
                        SoundEvents.VILLAGER_NO, SoundSource.PLAYERS,
                        1.0f, 1.0f);

                return InteractionResult.FAIL;
            }

            // Unlock the disk
            diskData.unlockDisk(diskId);

            // Show success message
            UpgradeDisk disk = DiskRegistry.getDisk(diskId);
            player.displayClientMessage(
                    Component.literal("Unlocked: ")
                            .append(Component.literal(disk.getDisplayName())
                                    .withStyle(style -> style.withColor(disk.getRarity().getColor()))),
                    true
            );

            // Play success sound
            level.playSound(null, player.blockPosition(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
                    1.0f, 1.5f);

            // TODO: Grant advancement here

            // Consume the item
            stack.shrink(1);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.CONSUME;
    }

    private static Rarity getRarityForDisk(String diskId) {
        UpgradeDisk disk = DiskRegistry.getDisk(diskId);
        if (disk == null) return Rarity.COMMON;

        return switch (disk.getRarity()) {
            case BASIC -> Rarity.COMMON;
            case RARE -> Rarity.UNCOMMON;
            case EPIC -> Rarity.RARE;
            case LEGENDARY, MYTHIC -> Rarity.EPIC;
        };
    }
}