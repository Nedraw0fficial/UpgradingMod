package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.TreasureSenseDisk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class TreasureSenseHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onChestOpen(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        AbstractContainerMenu container = event.getContainer();

        // Only process on server side
        if (player.level().isClientSide) return;

        // Check if it's a chest menu
        if (!(container instanceof ChestMenu chestMenu)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("treasure_sense")) {
                var disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof TreasureSenseDisk treasureDisk) {
                    int level = diskData.getDiskLevel(diskId);

                    float chance = treasureDisk.getDuplicationChance(level);

                    // Roll for duplication
                    if (RANDOM.nextFloat() < chance) {
                        int duplicateCount = treasureDisk.getDuplicationCount(level);

                        // Get the chest container (the actual chest inventory)
                        var chestContainer = chestMenu.getContainer();

                        // Find non-empty slots in chest
                        List<Integer> nonEmptySlots = new ArrayList<>();
                        for (int i = 0; i < chestContainer.getContainerSize(); i++) {
                            if (!chestContainer.getItem(i).isEmpty()) {
                                nonEmptySlots.add(i);
                            }
                        }

                        if (!nonEmptySlots.isEmpty()) {
                            // Duplicate random items
                            for (int i = 0; i < duplicateCount && i < nonEmptySlots.size(); i++) {
                                int randomSlot = nonEmptySlots.get(RANDOM.nextInt(nonEmptySlots.size()));
                                ItemStack original = chestContainer.getItem(randomSlot);

                                if (!original.isEmpty()) {
                                    // Find empty slot to put duplicate
                                    for (int emptySlot = 0; emptySlot < chestContainer.getContainerSize(); emptySlot++) {
                                        if (chestContainer.getItem(emptySlot).isEmpty()) {
                                            chestContainer.setItem(emptySlot, original.copy());
                                            System.out.println("DEBUG: Duplicated " + original.getDisplayName().getString());
                                            break;
                                        }
                                    }
                                }

                                // Remove from list so we don't duplicate the same item twice
                                nonEmptySlots.remove(Integer.valueOf(randomSlot));
                            }
                        }
                    }
                }

                return;
            }
        }
    }
}