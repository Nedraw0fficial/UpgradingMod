package com.nedraw.upgrading;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(UpgradingMod.MODID)
public class UpgradingMod {
    public static final String MODID = "upgrading";
    public static final Logger LOGGER = LogUtils.getLogger();

    public UpgradingMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("UPGRADING! Mod initializing...");
    }
}