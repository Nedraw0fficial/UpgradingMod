package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.item.ZSlotItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class ChipTriggerHandler {


    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        if (BoardEffectHandler.isBoardEffectActive(player.getUUID())) return;

        triggerChip(player, "heart");
    }


    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        if (BoardEffectHandler.isBoardEffectActive(player.getUUID())) return;

        triggerChip(player, "diamond");
    }


    @SubscribeEvent
    public static void onDamageDealt(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (event.getEntity() == player) return; // don't trigger on self-damage
        if (player.level().isClientSide) return;
        if (BoardEffectHandler.isBoardEffectActive(player.getUUID())) return;

        triggerChip(player, "spade");
    }


    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        if (BoardEffectHandler.isBoardEffectActive(player.getUUID())) return;

        triggerChip(player, "club");
    }


    @SubscribeEvent
    public static void onEat(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getItem().has(net.minecraft.core.component.DataComponents.FOOD)) return;
        if (player.level().isClientSide) return;
        if (BoardEffectHandler.isBoardEffectActive(player.getUUID())) return;

        triggerChip(player, "food");
    }



    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (BoardEffectHandler.isBoardEffectActive(player.getUUID())) return;

        triggerChip(player, "portal");
    }

    private static void triggerChip(ServerPlayer player, String chipType) {
        PlayerDiskData data = PlayerDiskData.get(player);


        String[] slotSnapshot = new String[3];
        for (int s = 0; s < 3; s++) slotSnapshot[s] = data.getEquippedDisk(s);

        for (int slot = 0; slot < 3; slot++) {
            if (slotSnapshot[slot] == null) continue;

            ItemStack zSlot = data.getZSlot(slot);
            if (zSlot.isEmpty()) continue;

            String chip = ZSlotItem.getChip(zSlot);
            if (!chip.equals(chipType)) continue;

            BoardEffectHandler.applyBoardEffect(player, slot);
        }
    }
}