package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class MountainGoatHandler {

    public static final Map<UUID, Long> CLING_START = new HashMap<>();
    public static final Map<UUID, Direction> CLING_WALL = new HashMap<>();
    public static final Map<UUID, Boolean> CLING_EXPIRED = new HashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide) return;

        PlayerDiskData data = PlayerDiskData.get(player);
        UpgradeDisk disk = null;
        int level = 0;
        int foundSlot = -1;

        for (int slot = 0; slot < 3; slot++) {
            String diskId = data.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("mountain_goat")) {
                disk = DiskRegistry.getDisk(diskId);
                level = data.getDiskLevel(diskId);
                foundSlot = slot;
                break;
            }
        }

        if (disk == null || level < 7) {
            UUID id = player.getUUID();
            CLING_START.remove(id);
            CLING_WALL.remove(id);
            CLING_EXPIRED.remove(id);
            return;
        }

        float efficiency = ZSlotEffects.getEfficiencyMultiplier(player, foundSlot);
        handleCling(player, level, efficiency);
    }

    private static void handleCling(Player player, int level, float efficiency) {
        UUID id = player.getUUID();
        long now = System.currentTimeMillis();

        if (player.onGround()) {
            CLING_START.remove(id);
            CLING_WALL.remove(id);
            CLING_EXPIRED.remove(id);
            return;
        }

        if (CLING_EXPIRED.getOrDefault(id, false)) return;

        if (player.isInWater() || player.getDeltaMovement().y >= 0) {
            CLING_START.remove(id);
            CLING_WALL.remove(id);
            return;
        }

        Direction wall = findWall(player);
        if (wall == null) {
            CLING_START.remove(id);
            CLING_WALL.remove(id);
            return;
        }

        if (!CLING_START.containsKey(id)) {
            CLING_START.put(id, now);
            CLING_WALL.put(id, wall);
        }

        long duration = (long)(getDuration(level) * efficiency * 1000);
        long elapsed = now - CLING_START.get(id);

        if (elapsed > duration) {
            CLING_START.remove(id);
            CLING_WALL.remove(id);
            CLING_EXPIRED.put(id, true);
            return;
        }

        player.setDeltaMovement(0, 0, 0);
        player.fallDistance = 0;
        player.hurtMarked = true;
    }

    public static void performWallJump(Player player, Direction wall) {
        UUID id = player.getUUID();
        CLING_START.remove(id);
        CLING_WALL.remove(id);
        CLING_EXPIRED.remove(id);

        Vec3 lookDir = player.getLookAngle();
        player.setDeltaMovement(lookDir.x * 0.9, 0.4, lookDir.z * 0.9);
        player.hurtMarked = true;

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.GOAT_LONG_JUMP, SoundSource.PLAYERS, 1.0f, 1.2f);

        if (player instanceof ServerPlayer sp) ModAdvancementTriggers.WALL_JUMP(sp);
    }

    private static double getDuration(int level) {
        return switch (level) {
            case 7 -> 1.0;   case 8 -> 1.25;  case 9 -> 1.5;
            case 10 -> 1.75; case 11 -> 2.0;  case 12 -> 2.5;
            default -> 0;
        };
    }

    private static Direction findWall(Player player) {
        Vec3 pos = player.position();
        if (solid(player, pos.x, pos.y+1, pos.z-0.4)) return Direction.NORTH;
        if (solid(player, pos.x, pos.y+1, pos.z+0.4)) return Direction.SOUTH;
        if (solid(player, pos.x-0.4, pos.y+1, pos.z)) return Direction.WEST;
        if (solid(player, pos.x+0.4, pos.y+1, pos.z)) return Direction.EAST;
        return null;
    }

    private static boolean solid(Player player, double x, double y, double z) {
        BlockState state = player.level().getBlockState(BlockPos.containing(x, y, z));
        return !state.isAir() && state.isSolid();
    }
}
