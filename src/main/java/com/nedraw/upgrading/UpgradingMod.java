package com.nedraw.upgrading;

import com.mojang.logging.LogUtils;
import com.nedraw.upgrading.client.ClientSetup;
import com.nedraw.upgrading.data.ModAttachments;
import com.nedraw.upgrading.disk.*;

import com.nedraw.upgrading.item.ModItems;
import com.nedraw.upgrading.network.ModNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.joml.sampling.PoissonSampling;
import org.slf4j.Logger;

@Mod(UpgradingMod.MODID)
public class UpgradingMod {
    public static final String MODID = "upgrading";
    private static final Logger LOGGER = LogUtils.getLogger();

    public UpgradingMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("UPGRADING! Mod initializing...");

        // Register disks FIRST
        LOGGER.info("Registering Upgrade Disks...");
        // --- DISKS ---
        // - BASIC -
        DiskRegistry.register(new SwiftFeetDisk());
        DiskRegistry.register(new SeaFishDisk());
        DiskRegistry.register(new MagnetDisk());
        DiskRegistry.register(new MightyMinerDisk());
        DiskRegistry.register(new NightVisionDisk());
        DiskRegistry.register(new LightweightDisk());
        DiskRegistry.register(new FeatherFallDisk());
        DiskRegistry.register(new EfficientDisk());
        DiskRegistry.register(new TankyDisk());
        // - RARE -
        DiskRegistry.register(new FlameWalkerDisk());
        DiskRegistry.register(new StepAssistDisk());
        DiskRegistry.register(new HarvesterDisk());
        DiskRegistry.register(new BeastWhispererDisk());
        DiskRegistry.register(new IronGripDisk());
        DiskRegistry.register(new TreasureSenseDisk());
        DiskRegistry.register(new PawnbrokerDisk());
        // - EPIC -
        DiskRegistry.register(new GluttonDisk());
        DiskRegistry.register(new SoapyHandsDisk());
        DiskRegistry.register(new MountainGoatDisk());
        // - LEGENDARY -
        DiskRegistry.register(new BerserkerDisk());
        DiskRegistry.register(new PyroclasmDisk());
        // - - - - - - -
        LOGGER.info("Registered {} disks", DiskRegistry.getAllDisks().size());

        // Register items
        ModItems.ITEMS.register(modEventBus);

        // Register tabs
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        // Register data attachments
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        // Register network packets
        modEventBus.addListener(ModNetwork::register);

        // Register common setup
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Common setup tasks (currently empty)
    }
}