package com.nedraw.upgrading.client;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.item.ModItems;
import com.nedraw.upgrading.particle.ModParticles;
import com.nedraw.upgrading.particle.NecromisisParticle;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import static com.nedraw.upgrading.client.ZSlotModelHandler.CHIPS;

@EventBusSubscriber(modid = UpgradingMod.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerKeyBinds(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBinds.OPEN_DISK_MENU);
        event.register(ModKeyBinds.ACTIVATE_SLOT_1);
        event.register(ModKeyBinds.ACTIVATE_SLOT_2);
        event.register(ModKeyBinds.ACTIVATE_SLOT_3);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(NecroArcherBowProperties::register);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.NECROMISIS.get(), NecromisisParticle.Provider::new);
    }

    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (String chip : CHIPS) {
            event.register(new ModelResourceLocation(
                    ResourceLocation.fromNamespaceAndPath(UpgradingMod.MODID, "item/chip_" + chip + "_zslot"),
                    "standalone"));
        }
    }

    public static void onModelsBaked(ModelEvent.ModifyBakingResult event) {
        ZSlotModelHandler.onModelsBaked(event);
    }

}