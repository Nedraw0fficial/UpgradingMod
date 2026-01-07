package com.nedraw.upgrading.item;

import com.nedraw.upgrading.client.screen.EncryptedFloppyScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EncryptedFloppyItem extends Item {

    public EncryptedFloppyItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            // Open the Encrypted Floppy GUI
            Minecraft.getInstance().setScreen(new EncryptedFloppyScreen());
        }
        //
        return InteractionResult.SUCCESS;
    }
}