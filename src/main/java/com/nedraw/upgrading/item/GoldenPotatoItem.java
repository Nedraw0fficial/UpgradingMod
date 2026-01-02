package com.nedraw.upgrading.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GoldenPotatoItem extends Item {
    public GoldenPotatoItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        // Can breed pigs like regular potato
        if (entity instanceof Pig) {
            return Items.POTATO.interactLivingEntity(stack, player, entity, hand);
        }
        return super.interactLivingEntity(stack, player, entity, hand);
    }
}