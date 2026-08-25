package com.nedraw.upgrading.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;

import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZSlotItem extends Item {

    private static final String FRAME_KEY = "upgrading_zslot_frame";
    private static final String BOARD_KEY = "upgrading_zslot_board";
    private static final String CHIP_KEY  = "upgrading_zslot_chip";

    private static final Pattern POSITIVE_PERCENT = Pattern.compile("\\+\\d+(\\.\\d+)?%");
    private static final Pattern NEGATIVE_PERCENT = Pattern.compile("-\\d+(\\.\\d+)?%");

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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        String frame = getFrame(stack);
        String board = getBoard(stack);
        String chip  = getChip(stack);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            if (Screen.hasShiftDown()) {
                Component keyName = com.nedraw.upgrading.client.ModKeyBinds.OPEN_DISK_MENU
                        .getKey().getDisplayName()
                        .copy().withStyle(s -> s.withBold(true).withUnderlined(true).withColor(0xFFFF55));

                tooltipComponents.add(
                        Component.literal("Press ")
                                .withStyle(s -> s.withColor(0xAAAAAA))
                                .append(keyName)
                                .append(Component.literal(" with this item")
                                        .withStyle(s -> s.withColor(0xAAAAAA)))
                );
                tooltipComponents.add(
                        Component.literal("in your hand and click a socket to equip it")
                                .withStyle(s -> s.withColor(0xAAAAAA))
                );
            } else {
                tooltipComponents.add(
                        Component.literal("[Shift] ???")
                                .withStyle(s -> s.withColor(0x888888).withItalic(true))
                );
            }
        }

        String frameDesc = strip(Component.translatable("tooltip.upgrading.frame." + frame).getString());
        tooltipComponents.add(colorizePercents(Component.literal("| " + frameDesc)));

        String boardDesc = stripBoard(Component.translatable("tooltip.upgrading.board." + board).getString());
        String chipDesc  = stripChip(Component.translatable("tooltip.upgrading.chip." + chip).getString());
        tooltipComponents.add(colorizePercents(Component.literal("| " + boardDesc + " " + chipDesc)));
    }

    private static String strip(String raw) {
        if (raw.startsWith(" >>")) return raw.substring(3).trim();
        return raw.trim();
    }

    private static String stripBoard(String raw) {
        String s = strip(raw);
        if (s.startsWith("On trigger: ")) return s.substring("On trigger: ".length());
        return s;
    }

    private static String stripChip(String raw) {
        String s = strip(raw);
        if (s.startsWith("Triggers ")) return s.substring("Triggers ".length());
        return s;
    }

    private static Component colorizePercents(MutableComponent base) {
        String text = base.getString();
        MutableComponent result = Component.empty();
        int last = 0;

        TreeMap<Integer, int[]> matches = new TreeMap<>();
        Matcher pos = POSITIVE_PERCENT.matcher(text);
        Matcher neg = NEGATIVE_PERCENT.matcher(text);
        while (pos.find()) matches.put(pos.start(), new int[]{pos.start(), pos.end(), 1});
        while (neg.find()) if (!matches.containsKey(neg.start()))
            matches.put(neg.start(), new int[]{neg.start(), neg.end(), -1});

        for (int[] match : matches.values()) {
            if (last < match[0])
                result.append(Component.literal(text.substring(last, match[0]))
                        .withStyle(s -> s.withColor(0xCCCCCC)));
            int color = match[2] > 0 ? 0x55FF55 : 0xFF5555;
            result.append(Component.literal(text.substring(match[0], match[1]))
                    .withStyle(s -> s.withColor(color)));
            last = match[1];
        }
        if (last < text.length())
            result.append(Component.literal(text.substring(last))
                    .withStyle(s -> s.withColor(0xCCCCCC)));

        return result;
    }
}