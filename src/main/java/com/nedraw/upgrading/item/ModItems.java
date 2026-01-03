package com.nedraw.upgrading.item;

import com.nedraw.upgrading.UpgradingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(UpgradingMod.MODID);

    // Golden Crops
    public static final DeferredHolder<Item, Item> GOLDEN_WHEAT = ITEMS.registerItem(
            "golden_wheat",
            properties -> new GoldenWheatItem(properties.food(Foods.GOLDEN_CARROT))
    );

    public static final DeferredHolder<Item, Item> GOLDEN_POTATO = ITEMS.registerItem(
            "golden_potato",
            properties -> new GoldenPotatoItem(properties.food(Foods.GOLDEN_CARROT))
    );

    public static final DeferredHolder<Item, Item> GOLDEN_BEETROOT = ITEMS.registerItem(
            "golden_beetroot",
            properties -> new GoldenBeetrootItem(properties.food(Foods.GOLDEN_CARROT))
    );

    public static final DeferredHolder<Item, Item> GOLDEN_NETHER_WART = ITEMS.registerItem(
            "golden_nether_wart",
            properties -> new GoldenNetherWartItem(properties.food(Foods.GOLDEN_CARROT))
    );

    public static final DeferredHolder<Item, Item> GOLDEN_SWEET_BERRIES = ITEMS.registerItem(
            "golden_sweet_berries",
            properties -> new GoldenSweetBerriesItem(properties.food(Foods.GOLDEN_CARROT))
    );


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

    public static final DeferredHolder<Item, DiskItem> STEP_ASSIST_DISK = ITEMS.registerItem(
            "step_assist_disk",
            properties -> new DiskItem("step_assist", properties)
    );

    public static final DeferredHolder<Item, DiskItem> HARVESTER_DISK = ITEMS.registerItem(
            "harvester_disk",
            properties -> new DiskItem("harvester", properties)
    );

    //_epic

    public static final DeferredHolder<Item, DiskItem> GLUTTON_DISK = ITEMS.registerItem(
            "glutton_disk",
            properties -> new DiskItem("glutton", properties)
    );
}