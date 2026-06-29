package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.WarchemistDisk;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class WarchemistHandler {

    private static final Random RANDOM = new Random();

    private static final List<Holder<MobEffect>> POSITIVE_EFFECTS = List.of(
            MobEffects.REGENERATION,
            MobEffects.SATURATION,
            MobEffects.FIRE_RESISTANCE,
            MobEffects.DAMAGE_RESISTANCE,
            MobEffects.DAMAGE_BOOST,
            MobEffects.MOVEMENT_SPEED,
            MobEffects.JUMP,
            MobEffects.ABSORPTION
    );

    private static final List<Holder<MobEffect>> NEGATIVE_EFFECTS = List.of(
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.WEAKNESS,
            MobEffects.POISON,
            MobEffects.BLINDNESS,
            MobEffects.GLOWING,
            MobEffects.HUNGER
    );

    // Trigger 1: Player DEALS damage → gets a positive effect
    @SubscribeEvent
    public static void onDamageDealt(LivingDamageEvent.Post event) {
        // The attacker must be a player
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        // Server side only
        if (player.level().isClientSide) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("warchemist")) {
                var disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof WarchemistDisk) {
                    int level = diskData.getDiskLevel(diskId);
                    int durationTicks = getDurationTicks(level);
                    int amplifier = level >= 12 ? 1 : 0;

                    // Apply random positive effect to player
                    Holder<MobEffect> positiveEffect = POSITIVE_EFFECTS.get(RANDOM.nextInt(POSITIVE_EFFECTS.size()));
                    player.addEffect(new MobEffectInstance(positiveEffect, durationTicks, amplifier, false, true));
                }
                return;
            }
        }
    }

    // Trigger 2 (L12 only): Player RECEIVES damage → attacker gets a negative effect
    @SubscribeEvent
    public static void onDamageReceived(LivingDamageEvent.Post event) {
        // The one taking damage must be a player
        if (!(event.getEntity() instanceof Player player)) return;

        // Server side only
        if (player.level().isClientSide) return;

        // The attacker must be a LivingEntity
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("warchemist")) {
                var disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof WarchemistDisk) {
                    int level = diskData.getDiskLevel(diskId);

                    // Only at L12
                    if (level >= 12) {
                        applyNegativeEffect(attacker);
                    }
                }
                return;
            }
        }
    }

    private static void applyNegativeEffect(LivingEntity target) {
        Holder<MobEffect> effect = NEGATIVE_EFFECTS.get(RANDOM.nextInt(NEGATIVE_EFFECTS.size()));

        // If the target is undead, swap Poison → Wither
        if (target.isInvertedHealAndHarm() && effect == MobEffects.POISON) {
            effect = MobEffects.WITHER;
        }

        // 5 seconds = 100 ticks
        target.addEffect(new MobEffectInstance(effect, 100, 0, false, true));
    }

    private static int getDurationTicks(int level) {
        return switch (level) {
            case 7  -> 60;   // 3s
            case 8  -> 80;   // 4s
            case 9  -> 100;  // 5s
            case 10 -> 140;  // 7s
            case 11 -> 180;  // 9s
            case 12 -> 240;  // 12s
            default -> 60;
        };
    }
}