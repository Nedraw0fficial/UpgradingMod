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
import net.minecraft.world.level.Level;

public class DiskItem extends Item {
    private final String diskId;

    public DiskItem(String diskId, Properties properties) {
        super(properties);
        this.diskId = diskId;
    }

    // ... rest of the code stays the same

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
                        Component.translatable("message.upgrading.already_unlocked")
                                .withStyle(style -> style.withColor(0xFF5555)),
                        true
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
            if (disk != null) {
                player.displayClientMessage(
                        Component.translatable("message.upgrading.disk_unlocked", disk.getDisplayName())
                                .withStyle(style -> style.withColor(0x55FF55)),
                        true
                );
            }

            // Play success sound
            level.playSound(null, player.blockPosition(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
                    1.0f, 1.5f);

            // Consume the item
            stack.shrink(1);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.CONSUME;
    }
}