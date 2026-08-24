package com.nedraw.upgrading.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;

public class ZSlotItem extends Item {

    private static final String FRAME_KEY = "upgrading_zslot_frame";
    private static final String BOARD_KEY = "upgrading_zslot_board";
    private static final String CHIP_KEY  = "upgrading_zslot_chip";

    public ZSlotItem(Properties properties) {
        super(properties);
    }

    public static String getFrame(ItemStack stack) { return getComponent(stack, FRAME_KEY); }
    public static String getBoard(ItemStack stack) { return getComponent(stack, BOARD_KEY); }
    public static String getChip(ItemStack stack)  { return getComponent(stack, CHIP_KEY); }

    public static void setComponents(ItemStack stack, String frame, String board, String chip) {
        CompoundTag tag = stack.has(DataComponents.CUSTOM_DATA)
                ? stack.get(DataComponents.CUSTOM_DATA).copyTag()
                : new CompoundTag();
        tag.putString(FRAME_KEY, frame);
        tag.putString(BOARD_KEY, board);
        tag.putString(CHIP_KEY, chip);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean isMythic(ItemStack stack) {
        return "void".equals(getFrame(stack))
                && "corrupted".equals(getBoard(stack))
                && "dark".equals(getChip(stack));
    }

    private static String getComponent(ItemStack stack, String key) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return "basic";
        CompoundTag tag = customData.copyTag();
        return tag.contains(key) ? tag.getString(key) : "basic";
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        String frame = getFrame(stack);
        String board = getBoard(stack);
        String chip  = getChip(stack);

        tooltipComponents.add(Component.translatable("tooltip.upgrading.zslot.frame",
                        Component.translatable("item.upgrading.frame_" + frame))
                .withStyle(style -> style.withColor(0xCCCCCC)));
        tooltipComponents.add(Component.translatable("tooltip.upgrading.zslot.chip",
                        Component.translatable("item.upgrading.chip_" + chip))
                .withStyle(style -> style.withColor(0xCCCCCC)));
        tooltipComponents.add(Component.translatable("tooltip.upgrading.zslot.board",
                        Component.translatable("item.upgrading.board_" + board))
                .withStyle(style -> style.withColor(0xCCCCCC)));
    }
}