package com.nedraw.upgrading.item;

import com.nedraw.upgrading.client.screen.EncryptedFloppyScreen;
import com.nedraw.upgrading.disk.DiskRarity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class EncryptedFloppyItem extends Item {

    private static final String STARTING_RARITY_KEY = "upgrading_starting_rarity";
    private static final String AMOUNT_OF_CHANCES_KEY = "upgrading_amount_of_chances";

    public EncryptedFloppyItem(Properties properties) {
        super(properties);
    }

    public static DiskRarity getStartingRarity(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains(STARTING_RARITY_KEY)) {
                try {
                    return DiskRarity.valueOf(tag.getString(STARTING_RARITY_KEY));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return DiskRarity.BASIC;
    }

    public static int getAmountOfChances(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains(AMOUNT_OF_CHANCES_KEY)) {
                return tag.getInt(AMOUNT_OF_CHANCES_KEY);
            }
        }
        return 4;
    }

    public static void setComponents(ItemStack stack, DiskRarity startingRarity, int amountOfChances) {
        CompoundTag tag = stack.has(DataComponents.CUSTOM_DATA)
                ? stack.get(DataComponents.CUSTOM_DATA).copyTag()
                : new CompoundTag();
        tag.putString(STARTING_RARITY_KEY, startingRarity.name());
        tag.putInt(AMOUNT_OF_CHANCES_KEY, amountOfChances);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        DiskRarity startingRarity = getStartingRarity(stack);
        int chances = getAmountOfChances(stack);

        tooltipComponents.add(
                Component.translatable("tooltip.upgrading.encrypted_floppy.starts_at")
                        .withStyle(style -> style.withColor(0xAAAAAA))
                        .append(Component.literal(startingRarity.getDisplayName())
                                .withStyle(style -> style.withColor(startingRarity.getColor()).withBold(true)))
        );

        if (chances == 0) {
            tooltipComponents.add(
                    Component.translatable("tooltip.upgrading.encrypted_floppy.guaranteed")
                            .withStyle(style -> style.withColor(0xFFD700))
            );
        } else {
            tooltipComponents.add(
                    Component.translatable("tooltip.upgrading.encrypted_floppy.chances", chances)
                            .withStyle(style -> style.withColor(0xAAAAAA))
            );
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            DiskRarity startingRarity = getStartingRarity(stack);
            int amountOfChances = getAmountOfChances(stack);
            Minecraft.getInstance().setScreen(new EncryptedFloppyScreen(startingRarity, amountOfChances));
        }

        return InteractionResultHolder.success(stack);
    }
}