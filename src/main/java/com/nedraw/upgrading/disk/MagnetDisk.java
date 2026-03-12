package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MagnetDisk extends UpgradeDisk {

    // Track which level is currently applied to each player
    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    public MagnetDisk() {
        super("magnet", "Magnet", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        // Update tracking when level changes
        if (appliedLevel == null || appliedLevel != level) {
            APPLIED_LEVELS.put(playerId, level);
        }
        // No one-time setup needed for magnet
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        // Server-side only
        if (player.level().isClientSide) return;

        // Calculate radius based on level
        double radius;
        boolean instantPickup;
        boolean requiresCrouch;

        if (level < 12) {
            // Levels 1-11: Only works when crouching, +0.5 blocks per level
            radius = 0.5 + (level * 0.5);
            instantPickup = false;
            requiresCrouch = true;
        } else {
            // Level 12: 8 block radius, instant pickup, no crouch needed
            radius = 8.0;
            instantPickup = true;
            requiresCrouch = false;
        }

        // Check if conditions are met
        if (requiresCrouch && !player.isCrouching()) {
            return; // Not crouching when required
        }

        // Pull items towards player
        AABB searchBox = player.getBoundingBox().inflate(radius);
        List<ItemEntity> nearbyItems = player.level().getEntitiesOfClass(ItemEntity.class, searchBox);

        for (ItemEntity item : nearbyItems) {
            if (!item.isAlive()) continue;
            if (item.hasPickUpDelay()) continue;

            double distance = player.distanceTo(item);
            if (distance > radius) continue;

            if (instantPickup) {
                // Level 12: Instant pickup
                item.setPos(player.getX(), player.getY(), player.getZ());
            } else {
                // Levels 1-11: Pull items towards player
                double dx = player.getX() - item.getX();
                double dy = player.getY() - item.getY();
                double dz = player.getZ() - item.getZ();

                double pullStrength = 0.1;
                item.setDeltaMovement(
                        item.getDeltaMovement().x + dx * pullStrength,
                        item.getDeltaMovement().y + dy * pullStrength,
                        item.getDeltaMovement().z + dz * pullStrength
                );
            }
        }
    }

    @Override
    public void removeEffect(Player player) {
        APPLIED_LEVELS.remove(player.getUUID());
        // No permanent modifiers to remove
    }
}