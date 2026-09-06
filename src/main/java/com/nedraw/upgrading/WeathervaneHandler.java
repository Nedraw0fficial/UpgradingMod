package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class WeathervaneHandler {

    private static final Random RANDOM = new Random();
    private static final float LIGHTNING_CHANCE = 0.122f;

    private static final Map<UUID, Map<UUID, Integer>> LIGHTNING_STREAK = new HashMap<>();

    @SubscribeEvent
    public static void onAttack(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (player.level().isClientSide) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (!serverLevel.isThundering()) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (!"weathervane".equals(diskId)) continue;

            int level = diskData.getDiskLevel(diskId);
            if (level < 12) return;

            float efficiency = com.nedraw.upgrading.ZSlotEffects.getEfficiencyMultiplier(player, slot);

            if (RANDOM.nextFloat() < LIGHTNING_CHANCE * efficiency) {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
                if (bolt != null) {
                    bolt.moveTo(target.getX(), target.getY(), target.getZ());
                    bolt.setCause(player);
                    serverLevel.addFreshEntity(bolt);

                    UUID playerId = player.getUUID();
                    UUID targetId = target.getUUID();
                    Map<UUID, Integer> streaks = LIGHTNING_STREAK.computeIfAbsent(playerId, k -> new HashMap<>());
                    int streak = streaks.getOrDefault(targetId, 0) + 1;
                    streaks.put(targetId, streak);

                    if (streak >= 3) {
                        ModAdvancementTriggers.LIGHTNING_3_STREAK(player);
                        streaks.put(targetId, 0);
                    }
                }
            } else {
                LIGHTNING_STREAK.computeIfAbsent(player.getUUID(), k -> new HashMap<>())
                        .put(target.getUUID(), 0);
            }
            return;
        }
    }
}
