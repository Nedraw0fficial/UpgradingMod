package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class DeathHandler {

    private static final Map<UUID, DiskBackup> DISK_BACKUPS = new HashMap<>();

    private static class DiskBackup {
        Set<String> unlockedDisks;
        String slot0, slot1, slot2;
        Map<String, Integer> diskLevels;

        DiskBackup(PlayerDiskData data) {
            this.unlockedDisks = Set.copyOf(data.getUnlockedDisks());
            this.slot0 = data.getEquippedDisk(0);
            this.slot1 = data.getEquippedDisk(1);
            this.slot2 = data.getEquippedDisk(2);

            this.diskLevels = new HashMap<>();
            for (String diskId : unlockedDisks) {
                diskLevels.put(diskId, data.getDiskLevel(diskId));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(net.neoforged.neoforge.event.entity.living.LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        // Access through server instead of level
        boolean keepInventory = player.server.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);

        if (keepInventory) {
            DISK_BACKUPS.put(player.getUUID(), new DiskBackup(diskData));
        } else {
            // Drop ALL unlocked disks (not just equipped ones!)
            Set<String> unlockedDisks = diskData.getUnlockedDisks();

            for (String diskId : unlockedDisks) {
                ItemStack diskItem = getDiskItemStack(diskId);

                if (!diskItem.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(
                            player.level(),
                            player.getX(),
                            player.getY(),
                            player.getZ(),
                            diskItem
                    );
                    player.level().addFreshEntity(itemEntity);
                }
            }

            DISK_BACKUPS.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID playerId = player.getUUID();
        DiskBackup backup = DISK_BACKUPS.get(playerId);

        if (backup != null) {
            PlayerDiskData diskData = PlayerDiskData.get(player);

            for (String diskId : backup.unlockedDisks) {
                diskData.unlockDisk(diskId);
                Integer level = backup.diskLevels.get(diskId);
                if (level != null) {
                    diskData.setDiskLevel(diskId, level);
                }
            }

            if (backup.slot0 != null) diskData.equipDisk(backup.slot0, 0);
            if (backup.slot1 != null) diskData.equipDisk(backup.slot1, 1);
            if (backup.slot2 != null) diskData.equipDisk(backup.slot2, 2);

            DISK_BACKUPS.remove(playerId);

            ServerEvents.syncDiskData(player);
        }
    }

    private static ItemStack getDiskItemStack(String diskId) {
        return switch (diskId) {
            case "swift_feet" -> new ItemStack(ModItems.SWIFT_FEET_DISK.get());
            case "sea_fish" -> new ItemStack(ModItems.SEA_FISH_DISK.get());
            case "magnet" -> new ItemStack(ModItems.MAGNET_DISK.get());
            case "mighty_miner" -> new ItemStack(ModItems.MIGHTY_MINER_DISK.get());
            case "night_vision" -> new ItemStack(ModItems.NIGHT_VISION_DISK.get());
            case "lightweight" -> new ItemStack(ModItems.LIGHTWEIGHT_DISK.get());
            case "feather_fall" -> new ItemStack(ModItems.FEATHER_FALL_DISK.get());
            case "efficient" -> new ItemStack(ModItems.EFFICIENT_DISK.get());
            case "tanky" -> new ItemStack(ModItems.TANKY_DISK.get());
            case "flame_walker" -> new ItemStack(ModItems.FLAME_WALKER_DISK.get());
            case "step_assist" -> new ItemStack(ModItems.STEP_ASSIST_DISK.get());
            case "harvester" -> new ItemStack(ModItems.HARVESTER_DISK.get());
            case "beast_whisperer" -> new ItemStack(ModItems.BEAST_WHISPERER_DISK.get());
            case "iron_grip" -> new ItemStack(ModItems.IRON_GRIP_DISK.get());
            case "treasure_sense" -> new ItemStack(ModItems.TREASURE_SENSE_DISK.get());
            case "pawnbroker" -> new ItemStack(ModItems.PAWNBROKER_DISK.get());
            case "glutton" -> new ItemStack(ModItems.GLUTTON_DISK.get());
            case "soapy_hands" -> new ItemStack(ModItems.SOAPY_HANDS_DISK.get());
            case "mountain_goat" -> new ItemStack(ModItems.MOUNTAIN_GOAT_DISK.get());
            case "berserker" -> new ItemStack(ModItems.BERSERKER_DISK.get());
            case "pyroclasm" -> new ItemStack(ModItems.PYROCLASM_DISK.get());
            case "architects_mind" -> new ItemStack(ModItems.ARCHITECTS_MIND_DISK.get());
            default -> ItemStack.EMPTY;
        };
    }
}