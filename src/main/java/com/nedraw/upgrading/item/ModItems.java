package com.nedraw.upgrading.item;

import com.nedraw.upgrading.UpgradingMod;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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

    // Other Items
    public static final DeferredHolder<Item, EncryptedFloppyItem> ENCRYPTED_FLOPPY = ITEMS.registerItem(
            "encrypted_floppy",
            EncryptedFloppyItem::new
    );
    public static final DeferredHolder<Item, Item> ENCRYPTED_FRAGMENT = ITEMS.registerItem(
            "encrypted_fragment",
            Item::new
    );

    // =====================
    // FRAMES (stack 64)
    // =====================

    // Flat frames - UNCOMMON
    public static final DeferredHolder<Item, ZSlotComponentItem> FRAME_FABRIC = ITEMS.registerItem(
            "frame_fabric",
            properties -> new ZSlotComponentItem(ZSlotComponentType.FRAME, "fabric", properties.stacksTo(64).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> FRAME_WOODEN = ITEMS.registerItem(
            "frame_wooden",
            properties -> new ZSlotComponentItem(ZSlotComponentType.FRAME, "wooden", properties.stacksTo(64).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> FRAME_COPPER = ITEMS.registerItem(
            "frame_copper",
            properties -> new ZSlotComponentItem(ZSlotComponentType.FRAME, "copper", properties.stacksTo(64).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> FRAME_IRON = ITEMS.registerItem(
            "frame_iron",
            properties -> new ZSlotComponentItem(ZSlotComponentType.FRAME, "iron", properties.stacksTo(64).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> FRAME_GOLDEN = ITEMS.registerItem(
            "frame_golden",
            properties -> new ZSlotComponentItem(ZSlotComponentType.FRAME, "golden", properties.stacksTo(64).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> FRAME_AMETHYST = ITEMS.registerItem(
            "frame_amethyst",
            properties -> new ZSlotComponentItem(ZSlotComponentType.FRAME, "amethyst", properties.stacksTo(64).rarity(Rarity.UNCOMMON))
    );

    // Special frames - RARE
    public static final DeferredHolder<Item, ZSlotComponentItem> FRAME_CACTUS = ITEMS.registerItem(
            "frame_cactus",
            properties -> new ZSlotComponentItem(ZSlotComponentType.FRAME, "cactus", properties.stacksTo(64).rarity(Rarity.RARE))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> FRAME_GLASS = ITEMS.registerItem(
            "frame_glass",
            properties -> new ZSlotComponentItem(ZSlotComponentType.FRAME, "glass", properties.stacksTo(64).rarity(Rarity.RARE))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> FRAME_ROSE_GOLD = ITEMS.registerItem(
            "frame_rose_gold",
            properties -> new ZSlotComponentItem(ZSlotComponentType.FRAME, "rose_gold", properties.stacksTo(64).rarity(Rarity.RARE))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> FRAME_SPONGE = ITEMS.registerItem(
            "frame_sponge",
            properties -> new ZSlotComponentItem(ZSlotComponentType.FRAME, "sponge", properties.stacksTo(64).rarity(Rarity.RARE))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> FRAME_MUSHROOM = ITEMS.registerItem(
            "frame_mushroom",
            properties -> new ZSlotComponentItem(ZSlotComponentType.FRAME, "mushroom", properties.stacksTo(64).rarity(Rarity.RARE))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> FRAME_VOID = ITEMS.registerItem(
            "frame_void",
            properties -> new ZSlotComponentItem(ZSlotComponentType.FRAME, "void", properties.stacksTo(64).rarity(Rarity.RARE))
    );

    // =====================
    // BOARDS (stack 32)
    // =====================

    public static final DeferredHolder<Item, ZSlotComponentItem> BOARD_BASIC = ITEMS.registerItem(
            "board_basic",
            properties -> new ZSlotComponentItem(ZSlotComponentType.BOARD, "basic", properties.stacksTo(32).rarity(Rarity.COMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> BOARD_ENDER = ITEMS.registerItem(
            "board_ender",
            properties -> new ZSlotComponentItem(ZSlotComponentType.BOARD, "ender", properties.stacksTo(32).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> BOARD_ENCHANTED = ITEMS.registerItem(
            "board_enchanted",
            properties -> new ZSlotComponentItem(ZSlotComponentType.BOARD, "enchanted", properties.stacksTo(32).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> BOARD_PISTON = ITEMS.registerItem(
            "board_piston",
            properties -> new ZSlotComponentItem(ZSlotComponentType.BOARD, "piston", properties.stacksTo(32).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> BOARD_WOOL = ITEMS.registerItem(
            "board_wool",
            properties -> new ZSlotComponentItem(ZSlotComponentType.BOARD, "wool", properties.stacksTo(32).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> BOARD_CORRUPTED = ITEMS.registerItem(
            "board_corrupted",
            properties -> new ZSlotComponentItem(ZSlotComponentType.BOARD, "corrupted", properties.stacksTo(32).rarity(Rarity.RARE))
    );

    // =====================
    // CHIPS (stack 16)
    // =====================

    public static final DeferredHolder<Item, ZSlotComponentItem> CHIP_BASIC = ITEMS.registerItem(
            "chip_basic",
            properties -> new ZSlotComponentItem(ZSlotComponentType.CHIP, "basic", properties.stacksTo(16).rarity(Rarity.COMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> CHIP_HEART = ITEMS.registerItem(
            "chip_heart",
            properties -> new ZSlotComponentItem(ZSlotComponentType.CHIP, "heart", properties.stacksTo(16).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> CHIP_DIAMOND = ITEMS.registerItem(
            "chip_diamond",
            properties -> new ZSlotComponentItem(ZSlotComponentType.CHIP, "diamond", properties.stacksTo(16).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> CHIP_SPADE = ITEMS.registerItem(
            "chip_spade",
            properties -> new ZSlotComponentItem(ZSlotComponentType.CHIP, "spade", properties.stacksTo(16).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> CHIP_CLUB = ITEMS.registerItem(
            "chip_club",
            properties -> new ZSlotComponentItem(ZSlotComponentType.CHIP, "club", properties.stacksTo(16).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> CHIP_FOOD = ITEMS.registerItem(
            "chip_food",
            properties -> new ZSlotComponentItem(ZSlotComponentType.CHIP, "food", properties.stacksTo(16).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> CHIP_PORTAL = ITEMS.registerItem(
            "chip_portal",
            properties -> new ZSlotComponentItem(ZSlotComponentType.CHIP, "portal", properties.stacksTo(16).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, ZSlotComponentItem> CHIP_DARK = ITEMS.registerItem(
            "chip_dark",
            properties -> new ZSlotComponentItem(ZSlotComponentType.CHIP, "dark", properties.stacksTo(16).rarity(Rarity.RARE))
    );

    // =====================
    // Z-SLOT (stack 8)
    // =====================

    public static final DeferredHolder<Item, ZSlotItem> Z_SLOT = ITEMS.registerItem(
            "z_slot",
            properties -> new ZSlotItem(properties.stacksTo(8))
    );

    // =====================
    // DISK REGISTRY
    // =====================

    // BASIC
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
    public static final DeferredHolder<Item, DiskItem> NIGHT_VISION_DISK = ITEMS.registerItem(
            "night_vision_disk",
            properties -> new DiskItem("night_vision", properties)
    );
    public static final DeferredHolder<Item, DiskItem> LIGHTWEIGHT_DISK = ITEMS.registerItem(
            "lightweight_disk",
            properties -> new DiskItem("lightweight", properties)
    );
    public static final DeferredHolder<Item, DiskItem> FEATHER_FALL_DISK = ITEMS.registerItem(
            "feather_fall_disk",
            properties -> new DiskItem("feather_fall", properties)
    );
    public static final DeferredHolder<Item, DiskItem> EFFICIENT_DISK = ITEMS.registerItem(
            "efficient_disk",
            properties -> new DiskItem("efficient", properties)
    );
    public static final DeferredHolder<Item, DiskItem> TANKY_DISK = ITEMS.registerItem(
            "tanky_disk",
            properties -> new DiskItem("tanky", properties)
    );

    // RARE
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
    public static final DeferredHolder<Item, DiskItem> BEAST_WHISPERER_DISK = ITEMS.registerItem(
            "beast_whisperer_disk",
            properties -> new DiskItem("beast_whisperer", properties)
    );
    public static final DeferredHolder<Item, DiskItem> IRON_GRIP_DISK = ITEMS.registerItem(
            "iron_grip_disk",
            properties -> new DiskItem("iron_grip", properties)
    );
    public static final DeferredHolder<Item, DiskItem> TREASURE_SENSE_DISK = ITEMS.registerItem(
            "treasure_sense_disk",
            properties -> new DiskItem("treasure_sense", properties)
    );
    public static final DeferredHolder<Item, DiskItem> PAWNBROKER_DISK = ITEMS.registerItem(
            "pawnbroker_disk",
            properties -> new DiskItem("pawnbroker", properties)
    );

    // EPIC
    public static final DeferredHolder<Item, DiskItem> GLUTTON_DISK = ITEMS.registerItem(
            "glutton_disk",
            properties -> new DiskItem("glutton", properties)
    );
    public static final DeferredHolder<Item, DiskItem> SOAPY_HANDS_DISK = ITEMS.registerItem(
            "soapy_hands_disk",
            properties -> new DiskItem("soapy_hands", properties)
    );
    public static final DeferredHolder<Item, DiskItem> MOUNTAIN_GOAT_DISK = ITEMS.registerItem(
            "mountain_goat_disk",
            properties -> new DiskItem("mountain_goat", properties)
    );
    public static final DeferredHolder<Item, DiskItem> WARCHEMIST_DISK = ITEMS.registerItem(
            "warchemist_disk",
            properties -> new DiskItem("warchemist", properties)
    );
    public static final DeferredHolder<Item, DiskItem> BASHER_DISK = ITEMS.registerItem(
            "basher_disk",
            properties -> new DiskItem("basher", properties)
    );

    // LEGENDARY
    public static final DeferredHolder<Item, DiskItem> BERSERKER_DISK = ITEMS.registerItem(
            "berserker_disk",
            properties -> new DiskItem("berserker", properties)
    );
    public static final DeferredHolder<Item, DiskItem> PYROCLASM_DISK = ITEMS.registerItem(
            "pyroclasm_disk",
            properties -> new DiskItem("pyroclasm", properties)
    );
    public static final DeferredHolder<Item, DiskItem> ARCHITECTS_MIND_DISK = ITEMS.registerItem(
            "architects_mind_disk",
            properties -> new DiskItem("architects_mind", properties)
    );

    // MYTHIC
    public static final DeferredHolder<Item, DiskItem> NECRO_ARCHER_DISK = ITEMS.registerItem(
            "necro_archer_disk",
            properties -> new DiskItem("necro_archer", properties)
    );
}