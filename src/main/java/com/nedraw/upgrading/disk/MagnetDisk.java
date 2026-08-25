package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MagnetDisk extends UpgradeDisk {

    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    public MagnetDisk() {
        super("magnet", "Magnet", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level) {
        APPLIED_LEVELS.put(player.getUUID(), level);
    }

    @Override
    public void applyTickEffect(Player player, int level, int slot, float efficiency) {
        if (player.level().isClientSide) return;

        double radius;
        boolean instantPickup;
        boolean requiresCrouch;

        if (level < 12) {
            radius = (0.5 + (level * 0.5)) * efficiency;
            instantPickup = false;
            requiresCrouch = true;
        } else {
            radius = 8.0 * efficiency;
            instantPickup = true;
            requiresCrouch = false;
        }

        if (requiresCrouch && !player.isCrouching()) return;

        AABB searchBox = player.getBoundingBox().inflate(radius);
        List<ItemEntity> nearbyItems = player.level().getEntitiesOfClass(ItemEntity.class, searchBox);
        int itemsAffected = 0;

        for (ItemEntity item : nearbyItems) {
            if (!item.isAlive() || item.hasPickUpDelay() || player.distanceTo(item) > radius) continue;

            if (instantPickup) {
                item.setPos(player.getX(), player.getY(), player.getZ());
            } else {
                double dx = player.getX() - item.getX();
                double dy = player.getY() - item.getY();
                double dz = player.getZ() - item.getZ();
                item.setDeltaMovement(
                        item.getDeltaMovement().x + dx * 0.1,
                        item.getDeltaMovement().y + dy * 0.1,
                        item.getDeltaMovement().z + dz * 0.1);
            }
            itemsAffected++;
        }

        if (itemsAffected >= 20 && player instanceof ServerPlayer sp) {
            ModAdvancementTriggers.MAGNET_PICKUP_20(sp);
        }
    }

    @Override
    public void removeEffect(Player player) {
        APPLIED_LEVELS.remove(player.getUUID());
    }
}
