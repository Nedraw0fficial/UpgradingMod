package com.nedraw.upgrading.item;

import com.nedraw.upgrading.ServerEvents;
import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class DiskItem extends Item {
    private final String diskId;

    public DiskItem(String diskId, Properties properties) {
        super(properties);
        this.diskId = diskId;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.upgrading.disk");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // Get the disk info
        UpgradeDisk disk = DiskRegistry.getDisk(diskId);
        if (disk != null) {
            int rarityColor = disk.getRarity().getColor();

            tooltipComponents.add(
                    Component.literal("[" + disk.getRarity().name() + "]")
                            .withStyle(style -> style
                                    .withColor(rarityColor)
                                    .withBold(true)
                                    .withUnderlined(true))
                            .append(Component.literal(" - \"" + disk.getDisplayName() + "\"")
                                    .withStyle(style -> style
                                            .withBold(false)
                                            .withUnderlined(false)
                                            .withColor(TextColor.fromLegacyFormat(ChatFormatting.WHITE))))
            );
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // Get player's disk data
            PlayerDiskData diskData = PlayerDiskData.get(player);

            if (diskData.isDiskUnlocked(diskId)) {
                player.displayClientMessage(
                        Component.translatable("message.upgrading.already_unlocked")
                                .withStyle(style -> style.withColor(0xFF5555)),
                        true
                );

                level.playSound(null, player.blockPosition(),
                        SoundEvents.VILLAGER_NO, SoundSource.PLAYERS,
                        1.0f, 1.0f);

                return InteractionResultHolder.fail(stack);
            }

            diskData.unlockDisk(diskId);

            UpgradeDisk disk = DiskRegistry.getDisk(diskId);
            if (disk != null && player instanceof ServerPlayer serverPlayer) {
                switch (disk.getRarity()) {
                    case BASIC      -> ModAdvancementTriggers.UNLOCK_BASIC_DISK(serverPlayer);
                    case RARE       -> ModAdvancementTriggers.UNLOCK_RARE_DISK(serverPlayer);
                    case EPIC       -> ModAdvancementTriggers.UNLOCK_EPIC_DISK(serverPlayer);
                    case LEGENDARY  -> ModAdvancementTriggers.UNLOCK_LEGENDARY_DISK(serverPlayer);
                    case MYTHIC     -> ModAdvancementTriggers.UNLOCK_MYTHIC_DISK(serverPlayer);
                }
            }

            // SYNC TO CLIENT
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                ServerEvents.syncDiskData(serverPlayer);
            }

            //UpgradeDisk disk = DiskRegistry.getDisk(diskId);
            if (disk != null) {
                player.displayClientMessage(
                        Component.translatable("message.upgrading.disk_unlocked", disk.getDisplayName())
                                .withStyle(style -> style.withColor(0x55FF55)),
                        true
                );
            }

            level.playSound(null, player.blockPosition(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS,
                    1.0f, 1.5f);

            stack.shrink(1);

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.consume(stack);
    }
}