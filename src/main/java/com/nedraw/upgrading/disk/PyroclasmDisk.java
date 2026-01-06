package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PyroclasmDisk extends UpgradeDisk {

    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    // Track if explosion is ready (needs full health)
    private static final Map<UUID, Boolean> EXPLOSION_READY = new HashMap<>();
    private static final Map<UUID, Boolean> WAS_BELOW_THRESHOLD = new HashMap<>();

    public PyroclasmDisk() {
        super("pyroclasm", "Pyroclasm", DiskRarity.LEGENDARY);

        this.withDescription(9, "9% chance: ignite on hit\n9% chance: ignite attackers")
                .withDescription(10, "13% chance: ignite on hit\n13% chance: ignite attackers")
                .withDescription(11, "16% chance: ignite on hit\n16% chance: ignite attackers")
                .withDescription(12, "20% fire on hit/counter + Fire Shield:\nExplodes at <40% HP, deals damage,\nknockback, ignites. Recharges at full HP");
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        APPLIED_LEVELS.put(playerId, level);

        // Initialize explosion state
        if (!EXPLOSION_READY.containsKey(playerId)) {
            EXPLOSION_READY.put(playerId, true); // Start ready
        }
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        if (level < 12) return;

        UUID playerId = player.getUUID();
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPercent = (currentHealth / maxHealth) * 100;

        boolean isBelowThreshold = healthPercent < 40;
        boolean wasBelowThreshold = WAS_BELOW_THRESHOLD.getOrDefault(playerId, false);

        // Check if just dropped below 40% HP
        if (isBelowThreshold && !wasBelowThreshold) {
            // Just crossed threshold!
            boolean explosionReady = EXPLOSION_READY.getOrDefault(playerId, false);

            if (explosionReady) {
                // TRIGGER EXPLOSION!
                triggerFireShieldExplosion(player);
                EXPLOSION_READY.put(playerId, false); // Explosion used
            }
        }

        // Recharge explosion at full health
        if (currentHealth >= maxHealth) {
            EXPLOSION_READY.put(playerId, true);
        }

        // Update tracking
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
        // Find nearby entities within 6 blocks
        AABB searchBox = player.getBoundingBox().inflate(6.0);
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player && entity.isAlive() // Exclude shield owner!
        );

        for (LivingEntity entity : nearbyEntities) {
            double distance = entity.distanceTo(player);

            if (distance <= 6.0) {
                // Set on fire for 15 seconds (300 ticks)
                entity.setRemainingFireTicks(300);

                // Deal fire damage (7 damage = 3.5 hearts)
                entity.hurt(player.damageSources().onFire(), 7.0f);

                // Knockback away from player
                double dx = entity.getX() - player.getX();
                double dz = entity.getZ() - player.getZ();
                double length = Math.sqrt(dx * dx + dz * dz);

                if (length > 0) {
                    // Normalize direction
                    dx /= length;
                    dz /= length;

                    // Stronger knockback = closer to player
                    double knockbackStrength = 3.0 - (distance * 0.3);
                    if (knockbackStrength < 0.5) knockbackStrength = 0.5;

                    entity.push(dx * knockbackStrength, 0.3, dz * knockbackStrength);
                }
            }
        }

        // Play firecharge sound
        player.level().playSound(
                null,
                player.blockPosition(),
                net.minecraft.sounds.SoundEvents.FIRECHARGE_USE,
                net.minecraft.sounds.SoundSource.PLAYERS,
                2.0f,
                0.8f
        );

        // Play fire ambient sound
        player.level().playSound(
                null,
                player.blockPosition(),
                net.minecraft.sounds.SoundEvents.FIRE_AMBIENT,
                net.minecraft.sounds.SoundSource.PLAYERS,
                1.5f,
                1.2f
        );

        // Spawn LOTS of flame particles in a burst!
        if (!player.level().isClientSide) {
            // Send particle packet to all nearby players
            for (int i = 0; i < 100; i++) {
                double angle = Math.random() * Math.PI * 2;
                double radius = Math.random() * 5.0;
                double px = player.getX() + Math.cos(angle) * radius;
                double py = player.getY() + Math.random() * 3.0;
                double pz = player.getZ() + Math.sin(angle) * radius;

                // Velocity pointing outward
                double vx = Math.cos(angle) * 0.3;
                double vy = 0.2;
                double vz = Math.sin(angle) * 0.3;

                ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                        net.minecraft.core.particles.ParticleTypes.FLAME,
                        px, py, pz,
                        1, // count
                        vx, vy, vz, // velocity
                        0.1 // speed
                );
            }

            // Add some lava particles too!
            for (int i = 0; i < 30; i++) {
                double angle = Math.random() * Math.PI * 2;
                double radius = Math.random() * 4.0;
                double px = player.getX() + Math.cos(angle) * radius;
                double py = player.getY() + Math.random() * 2.0;
                double pz = player.getZ() + Math.sin(angle) * radius;

                ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                        net.minecraft.core.particles.ParticleTypes.LAVA,
                        px, py, pz,
                        1, 0, 0.5, 0, 0.05
                );
            }
        }
    }

    // Get fire chance based on level
    public float getFireChance(int level) {
        return switch (level) {
            case 9 -> 0.09f;  // 9%
            case 10 -> 0.13f; // 13%
            case 11 -> 0.16f; // 16%
            default -> 0.20f; // 20% (level 12+)
        };
    }

    public boolean hasExplosionReady(UUID playerId) {
        return EXPLOSION_READY.getOrDefault(playerId, false);
    }

    public int getAppliedLevel(UUID playerId) {
        return APPLIED_LEVELS.getOrDefault(playerId, 0);
    }
}