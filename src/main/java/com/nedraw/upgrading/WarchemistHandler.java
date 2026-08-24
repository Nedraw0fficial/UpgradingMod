package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.WarchemistDisk;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
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
            MobEffects.GLOWING,
            MobEffects.HUNGER
    );

    @SubscribeEvent
    public static void onDamageReceived(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
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

                    // Apply random positive effect to player when hit
                    Holder<MobEffect> positiveEffect = POSITIVE_EFFECTS.get(RANDOM.nextInt(POSITIVE_EFFECTS.size()));
                    player.addEffect(new MobEffectInstance(positiveEffect, durationTicks, amplifier, false, true));

                    // Check advancement
                    if (player instanceof ServerPlayer sp && hasAllEightEffects(player)) {
                        ModAdvancementTriggers.ALL_8_EFFECTS(sp);
                    }

                    // Apply negative effect to attacker (L12 now applies at all levels for rework)
                    if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                        applyNegativeEffect(attacker, level);
                    }
                }
                return;
            }
        }
    }

    private static boolean hasAllEightEffects(Player player) {
        return player.hasEffect(MobEffects.REGENERATION) &&
                player.hasEffect(MobEffects.SATURATION) &&
                player.hasEffect(MobEffects.FIRE_RESISTANCE) &&
                player.hasEffect(MobEffects.DAMAGE_RESISTANCE) &&
                player.hasEffect(MobEffects.DAMAGE_BOOST) &&
                player.hasEffect(MobEffects.MOVEMENT_SPEED) &&
                player.hasEffect(MobEffects.JUMP) &&
                player.hasEffect(MobEffects.ABSORPTION);
    }

    private static void applyNegativeEffect(LivingEntity target, int level) {
        // Negative effect on attacker only at L12
        if (level < 12) return;

        Holder<MobEffect> effect = NEGATIVE_EFFECTS.get(RANDOM.nextInt(NEGATIVE_EFFECTS.size()));
        if (target.isInvertedHealAndHarm() && effect == MobEffects.POISON) {
            effect = MobEffects.WITHER;
        }
        target.addEffect(new MobEffectInstance(effect, 100, 0, false, true));
    }

    private static int getDurationTicks(int level) {
        return switch (level) {
            case 7  -> 60;
            case 8  -> 80;
            case 9  -> 100;
            case 10 -> 140;
            case 11 -> 180;
            case 12 -> 240;
            default -> 60;
        };
    }
}