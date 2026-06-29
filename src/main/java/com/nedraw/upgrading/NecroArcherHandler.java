package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.NecroArcherDisk;
import com.nedraw.upgrading.effect.ModEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class NecroArcherHandler {

    // NBT tag for Necromisis owner UUID on arrows
    public static final String NECRO_ARROW_TAG = "upgrading_necro_archer_owner";
    public static final String NECROMISIS_OWNER_TAG = "upgrading_necromisis_owner";

    // Per-player boost tracking: UUID -> boost end time (ms)
    private static final Map<UUID, Long> BOOST_END_TIMES = new HashMap<>();
    // Per-player boost level tracking
    private static final Map<UUID, Integer> BOOST_LEVELS = new HashMap<>();
    // Track whether detonation has been triggered for this boost cycle
    private static final Set<UUID> DETONATION_FIRED = new HashSet<>();

    // =====================
    // BOOST ACTIVATION
    // =====================

    public static void activateBoost(Player player, int level) {
        UUID id = player.getUUID();
        long boostDurationMs = level >= 12 ? 10_000L : 7_000L;

        BOOST_END_TIMES.put(id, System.currentTimeMillis() + boostDurationMs);
        BOOST_LEVELS.put(id, level);
        DETONATION_FIRED.remove(id); // Reset detonation flag

        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // Speed boost during active ability
        double speedBoost = level >= 12 ? 0.40 : 0.35;
        var speedAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            ResourceLocation boostId = ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "necro_archer_boost");
            speedAttr.removeModifier(boostId);
            speedAttr.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    boostId, speedBoost,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }

        // Visual + sound feedback (send to client via packet later for particles)
        serverPlayer.displayClientMessage(
                net.minecraft.network.chat.Component.literal("☠ Necromisis Activated!")
                        .withStyle(style -> style.withColor(0x1A1A1A).withBold(true)),
                true
        );

        serverPlayer.level().playSound(
                null, serverPlayer.blockPosition(),
                net.minecraft.sounds.SoundEvents.WITHER_SPAWN,
                net.minecraft.sounds.SoundSource.PLAYERS,
                0.6f, 1.8f
        );
    }

    public static boolean isPlayerBoosted(UUID playerId) {
        Long endTime = BOOST_END_TIMES.get(playerId);
        if (endTime == null) return false;
        return System.currentTimeMillis() < endTime;
    }

    // =====================
    // PLAYER TICK - handle boost expiry + detonation
    // =====================

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        UUID id = player.getUUID();
        Long endTime = BOOST_END_TIMES.get(id);
        if (endTime == null) return;

        long now = System.currentTimeMillis();

        // Boost just expired
        if (now >= endTime && !DETONATION_FIRED.contains(id)) {
            DETONATION_FIRED.add(id);
            BOOST_END_TIMES.remove(id);

            int level = BOOST_LEVELS.getOrDefault(id, 11);

            // Remove speed boost
            var speedAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                ResourceLocation boostId = ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "necro_archer_boost");
                speedAttr.removeModifier(boostId);
            }

            // DETONATION - find all entities with Necromisis tagged to this player
            if (player.level() instanceof ServerLevel serverLevel) {
                List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        new AABB(player.blockPosition()).inflate(512), // huge range - entire loaded area
                        entity -> {
                            if (!entity.hasEffect(ModEffects.NECROMISIS)) return false;
                            // Check if this Necromisis was applied by THIS player
                            var tag = entity.getPersistentData();
                            return tag.contains(NECROMISIS_OWNER_TAG) &&
                                    tag.getUUID(NECROMISIS_OWNER_TAG).equals(id);
                        }
                );

                for (LivingEntity target : targets) {
                    detonateTarget(player, target, level, serverLevel);
                }

                if (!targets.isEmpty()) {
                    player.level().playSound(
                            null, player.blockPosition(),
                            net.minecraft.sounds.SoundEvents.WITHER_DEATH,
                            net.minecraft.sounds.SoundSource.PLAYERS,
                            1.0f, 1.2f
                    );
                }
            }

            // Update cooldown - starts NOW (after boost ends)
            PlayerDiskData data = PlayerDiskData.get(player);
            data.setAbilityCooldown("necro_archer", now);
        }
    }

    private static void detonateTarget(Player owner, LivingEntity target, int level, ServerLevel level2) {
        // Half physical, half magic damage
        float totalDamage = level >= 12 ? 16.0f : 14.0f;
        float physicalDamage = totalDamage / 2f;
        float magicDamage = totalDamage / 2f;

        // Physical damage (goes through armor calculation)
        target.hurt(
                level2.damageSources().playerAttack((ServerPlayer) owner),
                physicalDamage
        );

        // Magic damage (bypasses armor)
        target.hurt(
                level2.damageSources().magic(),
                magicDamage
        );

        // Apply debuffs
        if (level >= 12) {
            // L12: Slowness III 16s, Weakness II 16s, Blindness III 8s
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 320, 2)); // III
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 320, 1));           // II
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 2));          // III
        } else {
            // L11: Slowness II 12s, Weakness II 12s, Blindness II 6s
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 1)); // II
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 240, 1));           // II
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 1));          // II
        }

        // Note: Necromisis effect STAYS on the target after detonation
    }

    // =====================
    // ARROW - Tag arrows shot by Necro-Archer players
    // =====================

    @SubscribeEvent
    public static void onArrowJoinWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getOwner() instanceof Player player)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId == null || !diskId.equals("necro_archer")) continue;

            var disk = DiskRegistry.getDisk(diskId);
            if (!(disk instanceof NecroArcherDisk)) continue;

            int level = diskData.getDiskLevel(diskId);

            // Tag the arrow with the owner's UUID for Necromisis application
            arrow.getPersistentData().putUUID(NECRO_ARROW_TAG, player.getUUID());

            // Apply damage multiplier
            float multiplier = getDamageMultiplier(player.getUUID(), level);
            arrow.setBaseDamage(arrow.getBaseDamage() * multiplier);

            // Apply velocity multiplier
            float velocityMult = getVelocityMultiplier(player.getUUID(), level);
            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(velocityMult));

            return;
        }
    }

    private static float getDamageMultiplier(UUID playerId, int level) {
        boolean inBoost = isPlayerBoosted(playerId);
        if (inBoost) {
            return level >= 12 ? 2.0f : 1.5f; // During boost same as passive (boost gives Necromisis)
        }
        return level >= 12 ? 2.0f : 1.5f;
    }

    private static float getVelocityMultiplier(UUID playerId, int level) {
        return level >= 12 ? 2.0f : 1.5f;
    }

    // =====================
    // ARROW HIT - Apply Necromisis during boost
    // =====================

    @SubscribeEvent
    public static void onArrowHit(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (event.getEntity().level().isClientSide) return;

        // Check if the damage came from a tagged Necro-Archer arrow
        DamageSource source = event.getSource();
        if (!(source.getDirectEntity() instanceof AbstractArrow arrow)) return;

        var arrowData = arrow.getPersistentData();
        if (!arrowData.contains(NECRO_ARROW_TAG)) return;

        UUID ownerId = arrowData.getUUID(NECRO_ARROW_TAG);

        // Only apply Necromisis during active boost
        if (!isPlayerBoosted(ownerId)) return;

        int level = BOOST_LEVELS.getOrDefault(ownerId, 11);
        int necromisisDuration = level >= 12 ? 12_000 : 6_000; // ticks: 600s or 300s

        // Apply Necromisis effect
        target.addEffect(new MobEffectInstance(
                ModEffects.NECROMISIS,
                necromisisDuration,
                0,
                false,
                true  // show particles
        ));

        // Tag the target with the owner's UUID
        target.getPersistentData().putUUID(NECROMISIS_OWNER_TAG, ownerId);
    }

    // =====================
    // NECROMISIS BONUS DAMAGE (+2 on hit)
    // =====================

    @SubscribeEvent
    public static void onNecromisisBonusDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;

        if (!target.hasEffect(ModEffects.NECROMISIS)) return;

        // Add 2 bonus damage to any hit received by Necromisis target
        event.setNewDamage(event.getNewDamage() + 2.0f);
    }

    // =====================
    // NECROMISIS BLOCKS REGEN
    // =====================

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        if (entity.hasEffect(ModEffects.NECROMISIS)) {
            // Cancel natural regeneration
            // Check if the heal source is natural regen (food/regen effect)
            event.setCanceled(true);
        }
    }
}