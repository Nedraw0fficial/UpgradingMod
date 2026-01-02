package com.nedraw.upgrading.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GoldenNetherWartItem extends Item {
    public GoldenNetherWartItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // When eaten, give XP to player
        if (!level.isClientSide && entity instanceof Player player) {
            // Give 50 XP (2.5 levels worth)
            player.giveExperiencePoints(50);

            // Play level up sound
            level.playSound(
                    null,
                    player.blockPosition(),
                    net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
                    net.minecraft.sounds.SoundSource.PLAYERS,
                    0.5f,
                    2.0f
            );
        }

        return super.finishUsingItem(stack, level, entity);
    }
}