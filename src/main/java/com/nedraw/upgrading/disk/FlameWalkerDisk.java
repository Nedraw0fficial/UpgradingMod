package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlameWalkerDisk extends UpgradeDisk {

    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();
    private static final Map<BlockPos, Long> TEMPORARY_MAGMA_BLOCKS = new HashMap<>();
    private static final long MAGMA_BLOCK_DURATION = 40;

    public FlameWalkerDisk() {
        super("flame_walker", "Flame Walker", DiskRarity.RARE);
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);
        if (appliedLevel == null || appliedLevel != level) APPLIED_LEVELS.put(playerId, level);
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        if (player.level().isClientSide) return;
        if (level >= 12 && player.level() instanceof ServerLevel serverLevel) {
            BlockPos playerPos = player.blockPosition();
            BlockPos belowPos = playerPos.below();
            BlockState belowState = serverLevel.getBlockState(belowPos);
            if (belowState.getBlock() == Blocks.LAVA && belowState.getFluidState().isSource()) {
                serverLevel.setBlock(belowPos, Blocks.MAGMA_BLOCK.defaultBlockState(), 3);
                TEMPORARY_MAGMA_BLOCKS.put(belowPos, serverLevel.getGameTime());
                if (player instanceof ServerPlayer sp) ModAdvancementTriggers.WALK_ON_LAVA(sp);
            }
            TEMPORARY_MAGMA_BLOCKS.entrySet().removeIf(entry -> {
                BlockPos pos = entry.getKey();
                long placedTime = entry.getValue();
                if (serverLevel.getGameTime() - placedTime >= MAGMA_BLOCK_DURATION) {
                    BlockState state = serverLevel.getBlockState(pos);
                    if (state.getBlock() == Blocks.MAGMA_BLOCK)
                        serverLevel.setBlock(pos, Blocks.LAVA.defaultBlockState(), 3);
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    public void removeEffect(Player player) {
        APPLIED_LEVELS.remove(player.getUUID());
    }

    public float reduceFireDamage(float originalDamage, int level, float efficiency) {
        float reduction = switch (level) {
            case 4  -> 0.04f; case 5  -> 0.08f; case 6  -> 0.12f;
            case 7  -> 0.16f; case 8  -> 0.20f; case 9  -> 0.24f;
            case 10 -> 0.28f; case 11 -> 0.33f; case 12 -> 0.40f;
            default -> 0.04f;
        };
        return originalDamage * (1.0f - Math.min(reduction * efficiency, 0.95f));
    }

    public int getAppliedLevel(UUID playerId) {
        return APPLIED_LEVELS.getOrDefault(playerId, 0);
    }
}
