package com.nedraw.upgrading.item;

import com.nedraw.upgrading.client.screen.EncryptedFloppyScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EncryptedFloppyItem extends Item {

    public EncryptedFloppyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);  // Get the stack first!

        if (level.isClientSide) {
            // Open the Encrypted Floppy GUI
            Minecraft.getInstance().setScreen(new EncryptedFloppyScreen());
        }

        return InteractionResultHolder.success(stack);  // Return the stack!
    }
}