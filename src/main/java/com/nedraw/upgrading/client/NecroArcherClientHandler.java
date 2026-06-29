package com.nedraw.upgrading.client;

import com.nedraw.upgrading.NecroArcherHandler;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.effect.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.Random;

@EventBusSubscriber(modid = UpgradingMod.MODID, value = Dist.CLIENT)
public class NecroArcherClientHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        LocalPlayer player = mc.player;

        // 1. Particles around the OWNER during boost
        if (NecroArcherHandler.isPlayerBoosted(player.getUUID())) {
            spawnBoostParticles(player);
        }

        // 2. Particles on ALL entities with Necromisis effect
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!living.hasEffect(ModEffects.NECROMISIS)) continue;
            spawnNecromisisParticles(living);
        }
    }

    private static void spawnBoostParticles(LocalPlayer player) {
        // Dark smoky particles swirling around the player during boost
        // Only spawn a few per tick to avoid performance issues
        for (int i = 0; i < 3; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 1.2;
            double offsetY = RANDOM.nextDouble() * 2.0;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 1.2;

            player.level().addParticle(
                    ParticleTypes.SQUID_INK, // Dark black ink particle - soulless look
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    (RANDOM.nextDouble() - 0.5) * 0.05,
                    0.02,
                    (RANDOM.nextDouble() - 0.5) * 0.05
            );
        }

        // Also add some ash/smoke
        for (int i = 0; i < 2; i++) {
            double offsetX = (RANDOM.nextDouble() - 0.5) * 0.8;
            double offsetY = RANDOM.nextDouble() * 2.0;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.8;

            player.level().addParticle(
                    ParticleTypes.ASH,
                    player.getX() + offsetX,
                    player.getY() + offsetY,
                    player.getZ() + offsetZ,
                    (RANDOM.nextDouble() - 0.5) * 0.02,
                    0.01,
                    (RANDOM.nextDouble() - 0.5) * 0.02
            );
        }
    }

    private static void spawnNecromisisParticles(LivingEntity entity) {
        // Only spawn occasionally to avoid spam
        if (RANDOM.nextInt(5) != 0) return;

        double offsetX = (RANDOM.nextDouble() - 0.5) * entity.getBbWidth();
        double offsetY = RANDOM.nextDouble() * entity.getBbHeight();
        double offsetZ = (RANDOM.nextDouble() - 0.5) * entity.getBbWidth();

        // Dark soul-like particles rising from affected entities
        entity.level().addParticle(
                ParticleTypes.SQUID_INK,
                entity.getX() + offsetX,
                entity.getY() + offsetY,
                entity.getZ() + offsetZ,
                0,
                0.03,
                0
        );
    }
}