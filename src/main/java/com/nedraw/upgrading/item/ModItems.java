package com.nedraw.upgrading.item;

import com.nedraw.upgrading.UpgradingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(UpgradingMod.MODID);

    // REGISTRY
    //_basic

    public static final DeferredHolder<Item, DiskItem> SWIFT_FEET_DISK = ITEMS.registerItem(
            "swift_feet_disk",
            properties -> new DiskItem("swift_feet", properties)
    );

    public static final DeferredHolder<Item, DiskItem> SEA_FISH_DISK = ITEMS.registerItem(
            "sea_fish_disk",
            properties -> new DiskItem("sea_fish", properties)
    );

    public static final DeferredHolder<Item, DiskItem> MAGNET_DISK = ITEMS.registerItem(
            "magnet_disk",
            properties -> new DiskItem("magnet", properties)
    );

    public static final DeferredHolder<Item, DiskItem> MIGHTY_MINER_DISK = ITEMS.registerItem(
            "mighty_miner_disk",
            properties -> new DiskItem("mighty_miner", properties)
    );

    //_rare

    public static final DeferredHolder<Item, DiskItem> FLAME_WALKER_DISK = ITEMS.registerItem(
            "flame_walker_disk",
            properties -> new DiskItem("flame_walker", properties)
    );
}