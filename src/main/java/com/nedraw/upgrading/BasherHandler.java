package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.BasherDisk;
import com.nedraw.upgrading.disk.DiskRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class BasherHandler {

    private static final float KNOCKBACK_CAP = 8.0f;

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        // Only trigger for players
        if (!(event.getEntity() instanceof Player player)) return;

        // Server side only
        if (player.level().isClientSide) return;

        // Only trigger when actually blocking
        if (!event.getBlocked()) return;

        // Only trigger for melee (attacker must be a LivingEntity)
        if (!(event.getDamageSource().getEntity() instanceof LivingEntity attacker)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("basher")) {
                var disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof BasherDisk) {
                    int level = diskData.getDiskLevel(diskId);
                    float blockedDamage = event.getBlockedDamage();
                    float knockbackMultiplier = getKnockbackMultiplier(level);

                    // Calculate knockback strength, capped at KNOCKBACK_CAP
                    float knockbackStrength = Math.min(blockedDamage * knockbackMultiplier, KNOCKBACK_CAP);

                    // Knockback direction = away from shield face (player's look direction)
                    Vec3 lookDir = player.getLookAngle();
                    attacker.setDeltaMovement(
                            attacker.getDeltaMovement().x + lookDir.x * knockbackStrength,
                            0.4,
                            attacker.getDeltaMovement().z + lookDir.z * knockbackStrength
                    );
                    attacker.hurtMarked = true;

                    // L12: return 30% of blocked damage as direct damage (ignores armor)
                    if (level >= 12) {
                        attacker.hurt(
                                player.damageSources().thorns(player),
                                blockedDamage * 0.30f
                        );
                    }
                }
                return;
            }
        }
    }

    private static float getKnockbackMultiplier(int level) {
        return switch (level) {
            case 7  -> 0.30f;
            case 8  -> 0.40f;
            case 9  -> 0.50f;
            case 10 -> 0.60f;
            case 11 -> 0.70f;
            case 12 -> 1.00f;
            default -> 0.30f;
        };
    }
}