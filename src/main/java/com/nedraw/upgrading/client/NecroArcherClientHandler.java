package com.nedraw.upgrading.client;

import com.nedraw.upgrading.NecroArcherHandler;
import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.effect.ModEffects;
import com.nedraw.upgrading.particle.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Random;

@EventBusSubscriber(modid = UpgradingMod.MODID, value = Dist.CLIENT)
public class NecroArcherClientHandler {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        LocalPlayer player = mc.player;

        // 1. Tornado around OWNER during boost
        if (NecroArcherHandler.isPlayerBoosted(player.getUUID())) {
            spawnBoostTornado(player);
        }

        // 2. Aura on ALL entities with Necromisis
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!living.hasEffect(ModEffects.NECROMISIS)) continue;
            spawnNecromisisTornado(living);
        }
    }

    private static void spawnBoostTornado(LocalPlayer player) {
        // 3 particles per tick (down from 5) — less cluttered
        // Biased toward the lower body using a squared random distribution
        for (int i = 0; i < 3; i++) {
            // squaring the random value biases spawn points toward 0 (feet)
            double t = Math.random();
            double spawnY = player.getY() + (t * t) * 1.8; // max 1.2 blocks up (roughly waist)

            player.level().addParticle(
                    ModParticles.NECROMISIS.get(),
                    player.getX(),
                    spawnY,
                    player.getZ(),
                    0, 0, 0
            );
        }
    }

    private static void spawnNecromisisTornado(LivingEntity entity) {
        // ~3 particles per second = 1 particle every 7 ticks
        if (RANDOM.nextInt(7) != 0) return;

        double t = Math.random();
        double spawnY = entity.getY() + (t * t) * (entity.getBbHeight() * 1.0);

        entity.level().addParticle(
                ModParticles.NECROMISIS.get(),
                entity.getX(),
                spawnY,
                entity.getZ(),
                0, 0, 0
        );
    }
}