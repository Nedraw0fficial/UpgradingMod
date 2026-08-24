package com.nedraw.upgrading.item;

import com.nedraw.upgrading.ServerEvents;
import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class DiskItem extends Item {
    private final String diskId;
    private static final String XP_LVL_KEY = "upgrading_xp_lvl";

    public DiskItem(String diskId, Properties properties) {
        super(properties);
        this.diskId = diskId;
    }

    public String getDiskId() {
        return this.diskId;
    }

    public static int getXpLvl(ItemStack stack, String diskId) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains(XP_LVL_KEY)) return tag.getInt(XP_LVL_KEY);
        }
        UpgradeDisk disk = DiskRegistry.getDisk(diskId);
        return disk != null ? disk.getRarity().getStartLevel() : 1;
    }

    public static void setXpLvl(ItemStack stack, int level) {
        CompoundTag tag = stack.has(DataComponents.CUSTOM_DATA)
                ? stack.get(DataComponents.CUSTOM_DATA).copyTag()
                : new CompoundTag();
        tag.putInt(XP_LVL_KEY, level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.upgrading.disk");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

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

            int xpLvl = getXpLvl(stack, diskId);
            if (xpLvl >= 12) {
                tooltipComponents.add(
                        Component.literal("MAXED OUT")
                                .withStyle(style -> style.withColor(0xFFD700).withBold(true))
                );
            } else {
                tooltipComponents.add(
                        Component.literal("Level " + xpLvl)
                                .withStyle(style -> style.withColor(0xFFFF55))
                );
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            PlayerDiskData diskData = PlayerDiskData.get(player);

            if (diskData.isDiskUnlocked(diskId)) {
                player.displayClientMessage(
                        Component.translatable("message.upgrading.already_unlocked")
                                .withStyle(style -> style.withColor(0xFF5555)),
                        true
                );

                level.playSound(player, player.blockPosition(),
                        SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);

                return InteractionResultHolder.fail(stack);
            }

            diskData.unlockDisk(diskId);

            int itemLevel = getXpLvl(stack, diskId);
            diskData.setDiskLevel(diskId, Math.min(itemLevel, 12));

            UpgradeDisk disk = DiskRegistry.getDisk(diskId);
            if (disk != null && player instanceof ServerPlayer serverPlayer) {
                switch (disk.getRarity()) {
                    case BASIC     -> ModAdvancementTriggers.UNLOCK_BASIC_DISK(serverPlayer);
                    case RARE      -> ModAdvancementTriggers.UNLOCK_RARE_DISK(serverPlayer);
                    case EPIC      -> ModAdvancementTriggers.UNLOCK_EPIC_DISK(serverPlayer);
                    case LEGENDARY -> ModAdvancementTriggers.UNLOCK_LEGENDARY_DISK(serverPlayer);
                    case MYTHIC    -> ModAdvancementTriggers.UNLOCK_MYTHIC_DISK(serverPlayer);
                }
            }

            if (player instanceof ServerPlayer serverPlayer) {
                ServerEvents.syncDiskData(serverPlayer);
            }

            if (disk != null) {
                player.displayClientMessage(
                        Component.translatable("message.upgrading.disk_unlocked", disk.getDisplayName())
                                .withStyle(style -> style.withColor(0x55FF55)),
                        true
                );
            }

            level.playSound(player, player.blockPosition(),
                    SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.5f);

            stack.shrink(1);

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.consume(stack);
    }
}