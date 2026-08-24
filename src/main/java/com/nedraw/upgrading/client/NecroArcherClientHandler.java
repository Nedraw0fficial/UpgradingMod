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
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        LocalPlayer player = mc.player;
        tickCounter++;

        // Boost tornado — only runs when player is boosted, which is rare
        if (NecroArcherHandler.isPlayerBoosted(player.getUUID())) {
            spawnBoostTornado(player);
        }

        // Only scan for Necromisis entities every 4 ticks instead of every tick
        // AND only if the local player has the Necro-Archer disk equipped
        if (tickCounter % 4 != 0) return;

        // Quick check: does the local player even have Necro-Archer equipped?
        // If not, skip the expensive entity scan entirely
        boolean hasNecroArcher = false;
        var data = com.nedraw.upgrading.data.PlayerDiskData.get(player);
        for (int slot = 0; slot < 3; slot++) {
            String diskId = data.getEquippedDisk(slot);
            if ("necro_archer".equals(diskId)) {
                hasNecroArcher = true;
                break;
            }
        }

        if (!hasNecroArcher) return;

        // Only now do we iterate entities
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (!living.hasEffect(ModEffects.NECROMISIS)) continue;
            spawnNecromisisTornado(living);
        }
    }

    private static void spawnBoostTornado(LocalPlayer player) {
        for (int i = 0; i < 3; i++) {
            double t = Math.random();
            double spawnY = player.getY() + (t * t) * 1.8;

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
        if (RANDOM.nextInt(2) != 0) return;

        double t = Math.random();
        double spawnY = entity.getY() + (t * t) * entity.getBbHeight();

        entity.level().addParticle(
                ModParticles.NECROMISIS.get(),
                entity.getX(),
                spawnY,
                entity.getZ(),
                0, 0, 0
        );
    }
}