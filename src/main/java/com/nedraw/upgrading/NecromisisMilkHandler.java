package com.nedraw.upgrading;

import com.nedraw.upgrading.effect.ModEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class NecromisisMilkHandler {

    @SubscribeEvent
    public static void onMilkFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        // Only care about milk buckets
        if (event.getItem().getItem() != Items.MILK_BUCKET) return;

        // If the player has Necromisis, punish the attempt to cure it
        if (player.hasEffect(ModEffects.NECROMISIS)) {
            player.hurt(player.damageSources().magic(), 2.0f);

            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("The curse rejects the cure...")
                            .withStyle(style -> style.withColor(0x1A1A1A)),
                    true
            );
        }
    }
}