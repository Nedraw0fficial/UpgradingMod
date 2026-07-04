package com.nedraw.upgrading.client;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.particle.ModParticles;
import com.nedraw.upgrading.particle.NecromisisParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerKeyBinds(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBinds.OPEN_DISK_MENU);
        event.register(ModKeyBinds.ACTIVATE_MYTHIC);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(NecroArcherBowProperties::register);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.NECROMISIS.get(), NecromisisParticle.Provider::new);
    }
}