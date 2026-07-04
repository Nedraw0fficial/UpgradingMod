package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PyroclasmDisk extends UpgradeDisk {

    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();
    private static final Map<UUID, Boolean> EXPLOSION_READY = new HashMap<>();
    private static final Map<UUID, Boolean> WAS_BELOW_THRESHOLD = new HashMap<>();

    public PyroclasmDisk() {
        super("pyroclasm", "Pyroclasm", DiskRarity.LEGENDARY);
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        APPLIED_LEVELS.put(playerId, level);
        if (!EXPLOSION_READY.containsKey(playerId)) {
            EXPLOSION_READY.put(playerId, true);
        }
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        if (level < 12) return;

        UUID playerId = player.getUUID();
        float healthPercent = (player.getHealth() / player.getMaxHealth()) * 100;
        boolean isBelowThreshold = healthPercent < 40;
        boolean wasBelowThreshold = WAS_BELOW_THRESHOLD.getOrDefault(playerId, false);

        if (isBelowThreshold && !wasBelowThreshold) {
            if (EXPLOSION_READY.getOrDefault(playerId, false)) {
                triggerFireShieldExplosion(player);
                EXPLOSION_READY.put(playerId, false);
            }
        }

        if (player.getHealth() >= player.getMaxHealth()) {
            EXPLOSION_READY.put(playerId, true);
        }

        WAS_BELOW_THRESHOLD.put(playerId, isBelowThreshold);
    }

    @Override
    public void removeEffect(Player player) {
        UUID playerId = player.getUUID();
        APPLIED_LEVELS.remove(playerId);
        EXPLOSION_READY.remove(playerId);
        WAS_BELOW_THRESHOLD.remove(playerId);
    }

    private void triggerFireShieldExplosion(Player player) {
        // Fire advancement when explosion triggers
        if (player instanceof ServerPlayer sp) {
            ModAdvancementTriggers.FIRE_SHIELD_EXPLODED(sp);
        }

        AABB searchBox = player.getBoundingBox().inflate(6.0);
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
                LivingEntity.class, searchBox,
                entity -> entity != player && entity.isAlive()
        );

        for (LivingEntity entity : nearbyEntities) {
            double distance = entity.distanceTo(player);
            if (distance <= 6.0) {
                entity.setRemainingFireTicks(300);
                entity.hurt(player.damageSources().onFire(), 7.0f);

                double dx = entity.getX() - player.getX();
                double dz = entity.getZ() - player.getZ();
                double length = Math.sqrt(dx * dx + dz * dz);

                if (length > 0) {
                    dx /= length;
                    dz /= length;
                    double knockbackStrength = Math.max(0.5, 3.0 - (distance * 0.3));
                    entity.push(dx * knockbackStrength, 0.3, dz * knockbackStrength);
                }
            }
        }

        player.level().playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.FIRECHARGE_USE,
                net.minecraft.sounds.SoundSource.PLAYERS, 2.0f, 0.8f);
        player.level().playSound(null, player.blockPosition(),
                net.minecraft.sounds.SoundEvents.FIRE_AMBIENT,
                net.minecraft.sounds.SoundSource.PLAYERS, 1.5f, 1.2f);

        if (!player.level().isClientSide) {
            net.minecraft.server.level.ServerLevel serverLevel =
                    (net.minecraft.server.level.ServerLevel) player.level();

            for (int i = 0; i < 100; i++) {
                double angle = Math.random() * Math.PI * 2;
                double radius = Math.random() * 5.0;
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.FLAME,
                        player.getX() + Math.cos(angle) * radius,
                        player.getY() + Math.random() * 3.0,
                        player.getZ() + Math.sin(angle) * radius,
                        1, Math.cos(angle) * 0.3, 0.2, Math.sin(angle) * 0.3, 0.1);
            }

            for (int i = 0; i < 30; i++) {
                double angle = Math.random() * Math.PI * 2;
                double radius = Math.random() * 4.0;
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.LAVA,
                        player.getX() + Math.cos(angle) * radius,
                        player.getY() + Math.random() * 2.0,
                        player.getZ() + Math.sin(angle) * radius,
                        1, 0, 0.5, 0, 0.05);
            }
        }
    }

    public float getFireChance(int level) {
        return switch (level) {
            case 9  -> 0.09f;
            case 10 -> 0.13f;
            case 11 -> 0.16f;
            default -> 0.20f;
        };
    }

    public boolean hasExplosionReady(UUID playerId) {
        return EXPLOSION_READY.getOrDefault(playerId, false);
    }

    public int getAppliedLevel(UUID playerId) {
        return APPLIED_LEVELS.getOrDefault(playerId, 0);
    }
}