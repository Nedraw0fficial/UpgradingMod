package com.nedraw.upgrading;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
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
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class NecroArcherHandler {

    public static final String NECRO_ARROW_TAG = "upgrading_necro_archer_owner";
    public static final String NECROMISIS_OWNER_TAG = "upgrading_necromisis_owner";

    private static final Map<UUID, Long> BOOST_END_TIMES = new HashMap<>();
    private static final Map<UUID, Integer> BOOST_LEVELS = new HashMap<>();
    private static final Set<UUID> DETONATION_FIRED = new HashSet<>();

    public static void activateBoost(Player player, int level, float efficiency) {
        UUID id = player.getUUID();
        long boostDurationMs = (long)((level >= 12 ? 10_000L : 7_000L) * efficiency);

        BOOST_END_TIMES.put(id, System.currentTimeMillis() + boostDurationMs);
        BOOST_LEVELS.put(id, level);
        DETONATION_FIRED.remove(id);

        if (!(player instanceof ServerPlayer serverPlayer)) return;

        double speedBoost = (level >= 12 ? 0.40 : 0.35) * efficiency;
        var speedAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            ResourceLocation boostId = ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "necro_archer_boost");
            speedAttr.removeModifier(boostId);
            speedAttr.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                    boostId, speedBoost,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        }

        serverPlayer.displayClientMessage(
                net.minecraft.network.chat.Component.literal("☠ Necromisis Activated!")
                        .withStyle(style -> style.withColor(0x1A1A1A).withBold(true)),
                true
        );

        serverPlayer.level().playSound(null, serverPlayer.blockPosition(),
                net.minecraft.sounds.SoundEvents.WITHER_SPAWN,
                net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1.8f);
    }

    public static void activateBoost(Player player, int level) {
        activateBoost(player, level, 1.0f);
    }

    public static boolean isPlayerBoosted(UUID playerId) {
        Long endTime = BOOST_END_TIMES.get(playerId);
        if (endTime == null) return false;
        return System.currentTimeMillis() < endTime;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        UUID id = player.getUUID();
        Long endTime = BOOST_END_TIMES.get(id);
        if (endTime == null) return;

        long now = System.currentTimeMillis();

        if (now >= endTime && !DETONATION_FIRED.contains(id)) {
            DETONATION_FIRED.add(id);
            BOOST_END_TIMES.remove(id);

            int level = BOOST_LEVELS.getOrDefault(id, 11);

            var speedAttr = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                ResourceLocation boostId = ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "necro_archer_boost");
                speedAttr.removeModifier(boostId);
            }

            if (player.level() instanceof ServerLevel serverLevel) {
                List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        new AABB(player.blockPosition()).inflate(512),
                        entity -> {
                            if (!entity.hasEffect(ModEffects.NECROMISIS)) return false;
                            var tag = entity.getPersistentData();
                            return tag.contains(NECROMISIS_OWNER_TAG) &&
                                    tag.getUUID(NECROMISIS_OWNER_TAG).equals(id);
                        }
                );

                // Fire advancement if 5+ enemies are detonated simultaneously
                if (targets.size() >= 5 && player instanceof ServerPlayer sp) {
                    ModAdvancementTriggers.NECROMISIS_DETONATED_5(sp);
                }

                for (LivingEntity target : targets) {
                    detonateTarget(player, target, level, serverLevel);
                }

                if (!targets.isEmpty()) {
                    player.level().playSound(null, player.blockPosition(),
                            net.minecraft.sounds.SoundEvents.WITHER_DEATH,
                            net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 1.2f);
                }
            }

            PlayerDiskData data = PlayerDiskData.get(player);
            data.setAbilityCooldown("necro_archer", now);
        }
    }

    private static void detonateTarget(Player owner, LivingEntity target, int level, ServerLevel level2) {
        float totalDamage = level >= 12 ? 10.0f : 8.0f;
        float half = totalDamage / 2f;

        target.hurt(level2.damageSources().playerAttack((ServerPlayer) owner), half);
        target.hurt(level2.damageSources().magic(), half);

        if (level >= 12) {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 320, 2));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 320, 1));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 160, 2));
        } else {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 240, 1));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 1));
        }
    }

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

            arrow.getPersistentData().putUUID(NECRO_ARROW_TAG, player.getUUID());

            float velocityMult = getVelocityMultiplier(player.getUUID(), level);
            var newMovement = arrow.getDeltaMovement().scale(velocityMult);
            arrow.setDeltaMovement(newMovement);

            double horizontalDistance = Math.sqrt(newMovement.x * newMovement.x + newMovement.z * newMovement.z);
            arrow.setYRot((float)(Math.atan2(newMovement.x, newMovement.z) * (180D / Math.PI)));
            arrow.setXRot((float)(Math.atan2(newMovement.y, horizontalDistance) * (180D / Math.PI)));
            arrow.yRotO = arrow.getYRot();
            arrow.xRotO = arrow.getXRot();
            return;
        }
    }

    private static float getVelocityMultiplier(UUID playerId, int level) {
        return level >= 12 ? 2.0f : 1.5f;
    }

    @SubscribeEvent
    public static void onArrowHit(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (event.getEntity().level().isClientSide) return;

        DamageSource source = event.getSource();
        if (!(source.getDirectEntity() instanceof AbstractArrow arrow)) return;

        var arrowData = arrow.getPersistentData();
        if (!arrowData.contains(NECRO_ARROW_TAG)) return;

        UUID ownerId = arrowData.getUUID(NECRO_ARROW_TAG);
        if (!isPlayerBoosted(ownerId)) return;

        int level = BOOST_LEVELS.getOrDefault(ownerId, 11);
        int necromisisDuration = level >= 12 ? 12_000 : 6_000;

        target.addEffect(new MobEffectInstance(ModEffects.NECROMISIS, necromisisDuration, 0, false, true));
        target.getPersistentData().putUUID(NECROMISIS_OWNER_TAG, ownerId);
    }

    @SubscribeEvent
    public static void onNecromisisBonusDamage(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        if (!target.hasEffect(ModEffects.NECROMISIS)) return;
        event.setNewDamage(event.getNewDamage() + 2.0f);
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (entity.hasEffect(ModEffects.NECROMISIS)) {
            event.setCanceled(true);
        }
    }
}