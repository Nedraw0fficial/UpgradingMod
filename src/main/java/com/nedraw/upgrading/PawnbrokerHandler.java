package com.nedraw.upgrading;

import com.nedraw.upgrading.UpgradingMod;
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

        // Check if player has Pawnbroker equipped
        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("pawnbroker")) {
                var disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof PawnbrokerDisk pawnbrokerDisk) {
                    int level = diskData.getDiskLevel(diskId);
                    double discount = pawnbrokerDisk.getDiscount(level);

                    // Get the trade offer
                    MerchantOffer offer = event.getMerchantOffer();

                    // Calculate emerald refund based on discount (FLOOR to prevent abuse)
                    int emeraldsToRefund = 0;

                    ItemStack costA = offer.getBaseCostA();
                    if (costA.is(Items.EMERALD)) {
                        int discountAmount = (int) Math.floor(costA.getCount() * discount);
                        emeraldsToRefund += discountAmount;
                    }

                    ItemStack costB = offer.getCostB();
                    if (!costB.isEmpty() && costB.is(Items.EMERALD)) {
                        int discountAmount = (int) Math.floor(costB.getCount() * discount);
                        emeraldsToRefund += discountAmount;
                    }

                    // Refund the discount
                    if (emeraldsToRefund > 0) {
                        ItemStack emeraldRefund = new ItemStack(Items.EMERALD, emeraldsToRefund);
                        player.getInventory().add(emeraldRefund);
                    }

                    // Level 12: 2% chance to refund ALL emeralds (on top of discount)
                    if (pawnbrokerDisk.canRefundEmeralds(level)) {
                        if (player.level().random.nextDouble() < pawnbrokerDisk.getRefundChance()) {
                            // Refund the FULL cost (not just discount)
                            int fullRefund = 0;

                            if (costA.is(Items.EMERALD)) {
                                fullRefund += costA.getCount();
                            }
                            if (!costB.isEmpty() && costB.is(Items.EMERALD)) {
                                fullRefund += costB.getCount();
                            }

                            // Refund the remaining emeralds (full cost - already refunded discount)
                            int remainingRefund = fullRefund - emeraldsToRefund;
                            if (remainingRefund > 0) {
                                ItemStack bonusRefund = new ItemStack(Items.EMERALD, remainingRefund);
                                player.getInventory().add(bonusRefund);
                            }

                            // Visual feedback
                            player.displayClientMessage(
                                    net.minecraft.network.chat.Component.literal("Lucky trade! Kept your emeralds!")
                                            .withStyle(style -> style.withColor(0x55FF55)),
                                    true
                            );
                        }
                    }
                }

                break;
            }
        }
    }
}