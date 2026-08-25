package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class ZSlotDeathHandler {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        boolean keepInventory = player.level().getGameRules()
                .getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY);
        if (keepInventory) return;

        PlayerDiskData data = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            ItemStack zSlot = data.getZSlot(slot);
            if (!zSlot.isEmpty()) {
                ItemEntity drop = new ItemEntity(
                        player.level(),
                        player.getX(), player.getY(), player.getZ(),
                        zSlot.copy()
                );
                player.level().addFreshEntity(drop);
                data.setZSlot(slot, ItemStack.EMPTY);
            }
        }
    }
}