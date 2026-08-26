package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.item.DiskItem;
import com.nedraw.upgrading.item.ModItems;
import com.nedraw.upgrading.item.ZSlotItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BoardEffectHandler {

    private static final Set<UUID> BOARD_EFFECT_ACTIVE = new HashSet<>();

    public static boolean isBoardEffectActive(UUID playerId) {
        return BOARD_EFFECT_ACTIVE.contains(playerId);
    }

    public static void applyBoardEffect(ServerPlayer player, int slot) {
        PlayerDiskData data = PlayerDiskData.get(player);
        ItemStack zSlot = data.getZSlot(slot);
        if (zSlot.isEmpty()) return;

        String board = ZSlotItem.getBoard(zSlot);
        UUID playerId = player.getUUID();

        BOARD_EFFECT_ACTIVE.add(playerId);
        try {
            switch (board) {
                case "ender"     -> applyEnderBoard(player, slot, data);
                case "enchanted" -> applyEnchantedBoard(player);
                case "piston"    -> applyPistonBoard(player, slot, data);
                case "wool"      -> applyWoolBoard(player);
                case "basic"     -> {}
                case "corrupted" -> {}
            }
        } finally {
            BOARD_EFFECT_ACTIVE.remove(playerId);
        }
    }


    private static void applyEnderBoard(ServerPlayer player, int slot, PlayerDiskData data) {
        String diskId = data.getEquippedDisk(slot);
        if (diskId == null) return;

        var disk = com.nedraw.upgrading.disk.DiskRegistry.getDisk(diskId);
        if (disk == null) return;

        int diskLevel = data.getDiskLevel(diskId);


        Item diskItem = ModItems.getDiskItem(diskId);
        if (diskItem == null) return;

        ItemStack diskStack = new ItemStack(diskItem);
        DiskItem.setXpLvl(diskStack, diskLevel);


        BlockPos spawnPos = player.getRespawnPosition();
        if (spawnPos == null) spawnPos = player.level().getSharedSpawnPos();

        net.minecraft.world.entity.item.ItemEntity itemEntity =
                new net.minecraft.world.entity.item.ItemEntity(
                        player.level(),
                        spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5,
                        diskStack);
        player.level().addFreshEntity(itemEntity);

        data.unequipSlot(slot);
        data.removeDisk(diskId);

        ServerEvents.syncDiskData(player);

        player.displayClientMessage(
                Component.literal("Your " + disk.getDisplayName() + " was sent to your spawn point!")
                        .withStyle(s -> s.withColor(0x5555FF)), true);

        player.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }


    private static void applyEnchantedBoard(ServerPlayer player) {
        player.giveExperiencePoints(10);
        player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
    }


    private static void applyPistonBoard(ServerPlayer player, int triggeredSlot, PlayerDiskData data) {
        String[] snap = new String[3];
        for (int s = 0; s < 3; s++) snap[s] = data.getEquippedDisk(s);

        String movingDisk = snap[triggeredSlot];
        if (movingDisk == null) return;

        int chainLength = 0;
        int checkSlot = triggeredSlot;
        while (snap[checkSlot] != null) {
            chainLength++;
            checkSlot = (checkSlot + 1) % 3;
            if (checkSlot == triggeredSlot) break;
        }

        for (int i = 0; i < chainLength; i++) {
            data.unequipSlot((triggeredSlot + i) % 3);
        }

        for (int i = 0; i < chainLength; i++) {
            int fromSlot = (triggeredSlot + i) % 3;
            int toSlot = (triggeredSlot + i + 1) % 3;
            if (snap[fromSlot] != null) {
                data.equipDisk(snap[fromSlot], toSlot);
            }
        }

        ServerEvents.syncDiskData(player);
        player.playSound(SoundEvents.PISTON_EXTEND, 0.5f, 1.0f);
    }

    private static void applyWoolBoard(ServerPlayer player) {
        player.heal(2.0f);
        player.playSound(SoundEvents.WOOL_PLACE, 0.8f, 1.0f);
    }
}