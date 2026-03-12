package com.nedraw.upgrading.disk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlameWalkerDisk extends UpgradeDisk {

    // Track which level is currently applied to each player
    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    // Track magma block positions created by the disk (to turn them back to lava)
    private static final Map<BlockPos, Long> TEMPORARY_MAGMA_BLOCKS = new HashMap<>();
    private static final long MAGMA_BLOCK_DURATION = 100; // 5 seconds (100 ticks)

    public FlameWalkerDisk() {
        super("flame_walker", "Flame Walker", DiskRarity.RARE);
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        // Update tracking
        if (appliedLevel == null || appliedLevel != level) {
            APPLIED_LEVELS.put(playerId, level);
        }

        // Fire resistance is handled in damage event
        // Lava walking is handled in tick effect
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        // Server-side only
        if (player.level().isClientSide) return;

        // Level 12: Walk on lava
        if (level >= 12 && player.level() instanceof ServerLevel serverLevel) {
            // Check blocks around player's feet
            BlockPos playerPos = player.blockPosition();
            BlockPos belowPos = playerPos.below();

            // Check if standing on/in lava
            BlockState belowState = serverLevel.getBlockState(belowPos);

            if (belowState.getBlock() == Blocks.LAVA && belowState.getFluidState().isSource()) {
                // Convert lava to temporary magma block
                serverLevel.setBlock(belowPos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);

                // Track this magma block with timestamp
                TEMPORARY_MAGMA_BLOCKS.put(belowPos, serverLevel.getGameTime());
            }

            // Clean up old magma blocks (turn back to lava)
            TEMPORARY_MAGMA_BLOCKS.entrySet().removeIf(entry -> {
                BlockPos pos = entry.getKey();
                long placedTime = entry.getValue();
                long currentTime = serverLevel.getGameTime();

                // If block has been there for duration, turn it back to lava
                if (currentTime - placedTime >= MAGMA_BLOCK_DURATION) {
                    BlockState state = serverLevel.getBlockState(pos);
                    // Only convert if it's still magma block (player might have mined it)
                    if (state.getBlock() == Blocks.MAGMA_BLOCK) {
                        serverLevel.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
                    }
                    return true; // Remove from tracking
                }
                return false;
            });
        }
    }

    @Override
    public void removeEffect(Player player) {
        APPLIED_LEVELS.remove(player.getUUID());
    }

    // Call this from damage event handler
    public float reduceFireDamage(float originalDamage, int level) {
        // Calculate levels above starting level (RARE starts at 4)
        int levelsAboveStart = level - 3; // RARE starts at level 4

        if (level >= 12) {
            // Level 12: 49% reduction (8 levels * 5.2% + special bonus)
            float reduction = 0.556f; // Use 'f' suffix for float
            return originalDamage * (1.0f - reduction);
        } else {
            // Levels 4-11: 5.2% per level ABOVE starting level
            float reduction = (levelsAboveStart * 5.2f) / 100.0f; // Use 'f' suffix for float
            return originalDamage * (1.0f - reduction);
        }
    }

    public int getAppliedLevel(UUID playerId) {
        return APPLIED_LEVELS.getOrDefault(playerId, 0);
    }
}