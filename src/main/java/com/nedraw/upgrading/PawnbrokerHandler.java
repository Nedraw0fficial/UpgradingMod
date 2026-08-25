package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.PawnbrokerDisk;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.TradeWithVillagerEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class PawnbrokerHandler {

    @SubscribeEvent
    public static void onVillagerTrade(TradeWithVillagerEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("pawnbroker")) {
                var disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof PawnbrokerDisk pawnbrokerDisk) {
                    int level = diskData.getDiskLevel(diskId);
                    float efficiency = ZSlotEffects.getEfficiencyMultiplier(player, slot);
                    double discount = pawnbrokerDisk.getDiscount(level, efficiency);

                    MerchantOffer offer = event.getMerchantOffer();

                    int emeraldsToRefund = 0;
                    ItemStack costA = offer.getBaseCostA();
                    if (costA.is(Items.EMERALD)) {
                        emeraldsToRefund += (int) Math.floor(costA.getCount() * discount);
                    }
                    ItemStack costB = offer.getCostB();
                    if (!costB.isEmpty() && costB.is(Items.EMERALD)) {
                        emeraldsToRefund += (int) Math.floor(costB.getCount() * discount);
                    }

                    if (emeraldsToRefund > 0) {
                        player.getInventory().add(new ItemStack(Items.EMERALD, emeraldsToRefund));
                    }

                    // Level 12: 2% chance to refund ALL emeralds
                    if (pawnbrokerDisk.canRefundEmeralds(level)) {
                        if (player.level().random.nextDouble() < pawnbrokerDisk.getRefundChance()) {
                            int fullRefund = 0;
                            if (costA.is(Items.EMERALD)) fullRefund += costA.getCount();
                            if (!costB.isEmpty() && costB.is(Items.EMERALD)) fullRefund += costB.getCount();

                            int remainingRefund = fullRefund - emeraldsToRefund;
                            if (remainingRefund > 0) {
                                player.getInventory().add(new ItemStack(Items.EMERALD, remainingRefund));
                            }

                            player.displayClientMessage(
                                    net.minecraft.network.chat.Component.literal("Lucky trade! Kept your emeralds!")
                                            .withStyle(style -> style.withColor(0x55FF55)),
                                    true
                            );

                            // Fire advancement - player kept their emeralds!
                            ModAdvancementTriggers.EMERALDS_KEPT(player);
                        }
                    }
                }
                break;
            }
        }
    }
}