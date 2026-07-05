package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class EncryptedFloppyHandler {

    @SubscribeEvent
    public static void onItemPickup(ItemEntityPickupEvent.Post event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        if (!event.getItemEntity().getItem().is(ModItems.ENCRYPTED_FLOPPY.get())) return;

        ModAdvancementTriggers.ROOT(player);
        ModAdvancementTriggers.HARDCODED(player);
    }
}