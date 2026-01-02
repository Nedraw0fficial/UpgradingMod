package com.nedraw.upgrading.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GoldenWheatItem extends Item {
    public GoldenWheatItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        // Can breed cows and sheep like regular wheat
        if (entity instanceof Cow || entity instanceof Sheep) {
            return net.minecraft.world.item.Items.WHEAT.interactLivingEntity(stack, player, entity, hand);
        }
        return super.interactLivingEntity(stack, player, entity, hand);
    }
}