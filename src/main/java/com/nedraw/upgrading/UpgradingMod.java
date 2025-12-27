package com.nedraw.upgrading;

import com.mojang.logging.LogUtils;
import com.nedraw.upgrading.data.ModAttachments;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.SwiftFeetDisk;
import com.nedraw.upgrading.item.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(UpgradingMod.MODID)
public class UpgradingMod {
    public static final String MODID = "upgrading";
    private static final Logger LOGGER = LogUtils.getLogger();

    public UpgradingMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("UPGRADING! Mod initializing...");

        // Register items
        ModItems.ITEMS.register(modEventBus);

        // Register data attachments
        ModAttachments.ATTACHMENT_TYPES.register(modEventBus);

        // Register common setup
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Register all disks here
        LOGGER.info("Registering Upgrade Disks...");

        DiskRegistry.register(new SwiftFeetDisk());

        LOGGER.info("Registered {} disks", DiskRegistry.getAllDisks().size());
    }
}