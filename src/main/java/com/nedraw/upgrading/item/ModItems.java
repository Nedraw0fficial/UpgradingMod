package com.nedraw.upgrading.item;

import com.nedraw.upgrading.UpgradingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, UpgradingMod.MODID);

    // Register disk items for each disk type
    public static final DeferredHolder<Item, Item> SWIFT_FEET_DISK = ITEMS.register(
            "swift_feet_disk",
            () -> new DiskItem("swift_feet")
    );

    // We'll add more disks here later:
    // public static final DeferredHolder<Item, Item> GARDENER_DISK = ...
}