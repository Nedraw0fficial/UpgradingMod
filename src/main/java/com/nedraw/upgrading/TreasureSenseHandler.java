package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.TreasureSenseDisk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class TreasureSenseHandler {

    private static final Random RANDOM = new Random();
    private static final String LOOTED_TAG = "upgrading_treasure_sense_looted";

    @SubscribeEvent
    public static void onChestOpen(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        AbstractContainerMenu container = event.getContainer();

        // Only process on server side
        if (player.level().isClientSide) return;

        // Check if it's a chest menu
        if (!(container instanceof ChestMenu chestMenu)) return;

        // Get the chest BlockEntity
        var chestContainer = chestMenu.getContainer();
        BlockEntity blockEntity = null;

        // Try to find the chest BlockEntity from the container
        if (chestContainer instanceof ChestBlockEntity chest) {
            blockEntity = chest;
        } else {
            // For double chests or other container types, we need to search nearby
            BlockPos playerPos = player.blockPosition();
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos checkPos = playerPos.offset(x, y, z);
                        BlockEntity be = player.level().getBlockEntity(checkPos);
                        if (be instanceof ChestBlockEntity) {
                            blockEntity = be;
                            break;
                        }
                    }
                }
            }
        }

        if (blockEntity == null) return;

        // Check if this chest was already looted by Treasure Sense
        var blockEntityData = blockEntity.getPersistentData();
        if (blockEntityData.contains(LOOTED_TAG)) {
            // Already looted, don't duplicate again
            return;
        }

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("treasure_sense")) {
                var disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof TreasureSenseDisk treasureDisk) {
                    int level = diskData.getDiskLevel(diskId);

                    // MARK CHEST AS LOOTED IMMEDIATELY - BEFORE THE ROLL
                    // This prevents save-scumming by opening/closing until you get lucky
                    blockEntityData.putBoolean(LOOTED_TAG, true);
                    blockEntity.setChanged();

                    float chance = treasureDisk.getDuplicationChance(level);

                    // Roll for duplication
                    if (RANDOM.nextFloat() < chance) {
                        int duplicateCount = treasureDisk.getDuplicationCount(level);

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