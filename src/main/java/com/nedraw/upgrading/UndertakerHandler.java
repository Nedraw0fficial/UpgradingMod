package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UndertakerDisk;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.*;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class UndertakerHandler {

    private static final Map<UUID, List<ItemStack>> KEPT_ITEMS = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> KEPT_DISKS = new HashMap<>();

    public static boolean shouldSkipDrop(UUID playerId, String diskId) {
        Map<String, Integer> kept = KEPT_DISKS.get(playerId);
        return kept != null && kept.containsKey(diskId);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().getGameRules().getBoolean(
                net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (!"undertaker".equals(diskId)) continue;

            var disk = DiskRegistry.getDisk(diskId);
            if (!(disk instanceof UndertakerDisk undertaker)) continue;

            int level = diskData.getDiskLevel(diskId);
            int baseItems = undertaker.getItemsToKeep(level);
            if (baseItems == 0) return;

            float bonus = ZSlotEffects.getBlueprintBonus(player, slot);
            int itemsToKeep = baseItems + Math.round(baseItems * bonus);

            UUID playerId = player.getUUID();

            List<int[]> nonEmptySlots = new ArrayList<>();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (!player.getInventory().getItem(i).isEmpty()) {
                    nonEmptySlots.add(new int[]{i});
                }
            }

            Collections.shuffle(nonEmptySlots);
            List<ItemStack> keptItems = new ArrayList<>();
            for (int i = 0; i < Math.min(itemsToKeep, nonEmptySlots.size()); i++) {
                int invSlot = nonEmptySlots.get(i)[0];
                keptItems.add(player.getInventory().getItem(invSlot).copy());
                player.getInventory().setItem(invSlot, ItemStack.EMPTY);
            }
            KEPT_ITEMS.put(playerId, keptItems);

            if (undertaker.keepsEquippedDisk(level)) {
                List<String> equippedDisks = new ArrayList<>();
                for (int s = 0; s < 3; s++) {
                    String equipped = diskData.getEquippedDisk(s);
                    if (equipped != null && !equippedDisks.contains(equipped)) equippedDisks.add(equipped);
                }

                Collections.shuffle(equippedDisks);
                int disksToKeep = 1 + (bonus >= 1.0f ? 1 : 0);

                Map<String, Integer> keptDisks = new HashMap<>();
                for (int i = 0; i < Math.min(disksToKeep, equippedDisks.size()); i++) {
                    String keptDiskId = equippedDisks.get(i);
                    keptDisks.put(keptDiskId, diskData.getDiskLevel(keptDiskId));

                    if ("undertaker".equals(keptDiskId)) {
                        ModAdvancementTriggers.DEATH_CAN_WAIT(player);
                    }
                }

                if (!keptDisks.isEmpty()) KEPT_DISKS.put(playerId, keptDisks);
            }
            return;
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID playerId = player.getUUID();

        List<ItemStack> keptItems = KEPT_ITEMS.remove(playerId);
        if (keptItems != null) {
            for (ItemStack stack : keptItems) {
                if (!player.addItem(stack)) player.drop(stack, false);
            }
        }

        Map<String, Integer> keptDisks = KEPT_DISKS.remove(playerId);
        if (keptDisks != null) {
            PlayerDiskData diskData = PlayerDiskData.get(player);
            for (Map.Entry<String, Integer> entry : keptDisks.entrySet()) {
                diskData.unlockDisk(entry.getKey());
                diskData.setDiskLevel(entry.getKey(), entry.getValue());
            }
            ServerEvents.syncDiskData(player);
        }
    }
}