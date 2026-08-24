package com.nedraw.upgrading.loot;

import com.nedraw.upgrading.UpgradingMod;
import com.nedraw.upgrading.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class DiskLootInjector {

    private record LootEntry(DeferredHolder<Item, ?> item, float chance, int minCount, int maxCount, boolean isDisk, boolean isFloppy) {}
    private static final Map<ResourceKey<LootTable>, List<LootEntry>> LOOT_INJECTIONS = new HashMap<>();

    static {

        // === ENCRYPTED FRAGMENTS ===

        addToLoot("gameplay/fishing/treasure",  ModItems.ENCRYPTED_FRAGMENT, 0.04f, 1, 1);
        addToLoot("gameplay/fishing/junk",      ModItems.ENCRYPTED_FRAGMENT, 0.08f, 1, 1);

        addToLoot("chests/simple_dungeon",                  ModItems.ENCRYPTED_FRAGMENT, 0.15f, 1, 2);
        addToLoot("chests/abandoned_mineshaft",             ModItems.ENCRYPTED_FRAGMENT, 0.15f, 1, 2);
        addToLoot("chests/village/village_weaponsmith",     ModItems.ENCRYPTED_FRAGMENT, 0.15f, 1, 2);
        addToLoot("chests/village/village_toolsmith",       ModItems.ENCRYPTED_FRAGMENT, 0.15f, 1, 2);

        addToLoot("chests/nether_bridge",                   ModItems.ENCRYPTED_FRAGMENT, 0.20f, 2, 3);
        addToLoot("chests/bastion_other",                   ModItems.ENCRYPTED_FRAGMENT, 0.20f, 2, 3);
        addToLoot("chests/desert_pyramid",                  ModItems.ENCRYPTED_FRAGMENT, 0.20f, 2, 3);
        addToLoot("chests/jungle_temple",                   ModItems.ENCRYPTED_FRAGMENT, 0.20f, 2, 3);
        addToLoot("chests/pillager_outpost",                ModItems.ENCRYPTED_FRAGMENT, 0.20f, 2, 3);
        addToLoot("chests/shipwreck_treasure",              ModItems.ENCRYPTED_FRAGMENT, 0.20f, 2, 3);
        addToLoot("chests/woodland_mansion",                ModItems.ENCRYPTED_FRAGMENT, 0.20f, 2, 3);

        addToLoot("chests/ancient_city",                    ModItems.ENCRYPTED_FRAGMENT, 0.30f, 3, 5);
        addToLoot("chests/end_city_treasure",               ModItems.ENCRYPTED_FRAGMENT, 0.30f, 3, 5);
        addToLoot("chests/bastion_treasure",                ModItems.ENCRYPTED_FRAGMENT, 0.30f, 3, 5);
        addToLoot("chests/trial_chambers/reward_common",    ModItems.ENCRYPTED_FRAGMENT, 0.30f, 3, 5);
        addToLoot("chests/ruined_portal", ModItems.ENCRYPTED_FRAGMENT, 0.30f, 3, 5);

        // === ENCRYPTED FLOPPY ===

        addFloppyToLoot("gameplay/fishing/treasure", ModItems.ENCRYPTED_FLOPPY, 0.06f);

        addFloppyToLoot("chests/village/village_plains_house", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_desert_house", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_savanna_house", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_taiga_house", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_snowy_house", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_armorer", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_weaponsmith", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_toolsmith", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_cartographer", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_mason", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_shepherd", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_butcher", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_fletcher", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_fisher", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_tannery", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/village/village_temple", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/simple_dungeon", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/abandoned_mineshaft", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/stronghold_corridor", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/stronghold_crossing", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/stronghold_library", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/desert_pyramid", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/jungle_temple", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/igloo_chest", ModItems.ENCRYPTED_FLOPPY, 0.20f);
        addFloppyToLoot("chests/pillager_outpost", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/woodland_mansion", ModItems.ENCRYPTED_FLOPPY, 0.20f);
        addFloppyToLoot("chests/shipwreck_treasure", ModItems.ENCRYPTED_FLOPPY, 0.20f);
        addFloppyToLoot("chests/shipwreck_supply", ModItems.ENCRYPTED_FLOPPY, 0.10f);
        addFloppyToLoot("chests/shipwreck_map", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/buried_treasure", ModItems.ENCRYPTED_FLOPPY, 0.27f);
        addFloppyToLoot("chests/underwater_ruin_small", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/underwater_ruin_big", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/nether_bridge", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/bastion_treasure", ModItems.ENCRYPTED_FLOPPY, 0.20f);
        addFloppyToLoot("chests/bastion_other", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/bastion_bridge", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/bastion_hoglin_stable", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addFloppyToLoot("chests/end_city_treasure", ModItems.ENCRYPTED_FLOPPY, 0.27f);
        addFloppyToLoot("chests/ancient_city", ModItems.ENCRYPTED_FLOPPY, 0.20f);
        addFloppyToLoot("chests/ancient_city_ice_box", ModItems.ENCRYPTED_FLOPPY, 0.20f);
        addFloppyToLoot("chests/trial_chambers/reward_rare", ModItems.ENCRYPTED_FLOPPY, 0.20f);
        addFloppyToLoot("chests/trial_chambers/reward_unique", ModItems.ENCRYPTED_FLOPPY, 0.25f);
        addFloppyToLoot("chests/trial_chambers/supply", ModItems.ENCRYPTED_FLOPPY, 0.10f);
        addFloppyToLoot("chests/trial_chambers/corridor", ModItems.ENCRYPTED_FLOPPY, 0.10f);
        addFloppyToLoot("chests/trial_chambers/entrance", ModItems.ENCRYPTED_FLOPPY, 0.20f);
        addFloppyToLoot("chests/trial_chambers/intersection", ModItems.ENCRYPTED_FLOPPY, 0.05f);


        // === FRAMES (FLAT) ===
        // Fabric - common chests
        addToLoot("chests/simple_dungeon",              ModItems.FRAME_FABRIC, 0.15f, 1, 2);
        addToLoot("chests/abandoned_mineshaft",         ModItems.FRAME_FABRIC, 0.15f, 1, 2);
        addToLoot("chests/village/village_toolsmith",   ModItems.FRAME_FABRIC, 0.20f, 1, 2);
        addToLoot("chests/village/village_weaponsmith", ModItems.FRAME_FABRIC, 0.20f, 1, 2);
        addToLoot("chests/jungle_temple",               ModItems.FRAME_FABRIC, 0.15f, 1, 2);
        addToLoot("chests/desert_pyramid",              ModItems.FRAME_FABRIC, 0.10f, 1, 2);

        // Wooden - uncommon chests
        addToLoot("chests/stronghold_corridor",         ModItems.FRAME_WOODEN, 0.10f, 1, 1);
        addToLoot("chests/stronghold_crossing",         ModItems.FRAME_WOODEN, 0.10f, 1, 1);
        addToLoot("chests/pillager_outpost",            ModItems.FRAME_WOODEN, 0.12f, 1, 1);
        addToLoot("chests/shipwreck_treasure",          ModItems.FRAME_WOODEN, 0.13f, 1, 1);

        // Copper - rare chests
        addToLoot("chests/nether_bridge",               ModItems.FRAME_COPPER, 0.09f, 1, 1);
        addToLoot("chests/bastion_other",               ModItems.FRAME_COPPER, 0.08f, 1, 1);
        addToLoot("chests/woodland_mansion",            ModItems.FRAME_COPPER, 0.07f, 1, 1);

        // === FRAMES (SPECIAL) ===
        addToLoot("chests/ancient_city",                ModItems.FRAME_CACTUS,    0.03f, 1, 1);
        addToLoot("chests/end_city_treasure",           ModItems.FRAME_GLASS,     0.03f, 1, 1);
        addToLoot("chests/bastion_treasure",            ModItems.FRAME_ROSE_GOLD, 0.03f, 1, 1);
        addToLoot("chests/trial_chambers/reward_unique",ModItems.FRAME_SPONGE,    0.03f, 1, 1);
        addToLoot("chests/ancient_city_ice_box",        ModItems.FRAME_MUSHROOM,  0.03f, 1, 1);

        // === BOARDS ===
        addToLoot("chests/simple_dungeon",              ModItems.BOARD_BASIC,     0.10f, 1, 1);
        addToLoot("chests/abandoned_mineshaft",         ModItems.BOARD_BASIC,     0.10f, 1, 1);
        addToLoot("chests/village/village_toolsmith",   ModItems.BOARD_BASIC,     0.10f, 1, 1);

        addToLoot("chests/nether_bridge",               ModItems.BOARD_ENDER,     0.08f, 1, 1);
        addToLoot("chests/bastion_other",               ModItems.BOARD_ENDER,     0.06f, 1, 1);

        addToLoot("chests/stronghold_library",          ModItems.BOARD_ENCHANTED, 0.08f, 1, 1);
        addToLoot("chests/woodland_mansion",            ModItems.BOARD_ENCHANTED, 0.07f, 1, 1);

        addToLoot("chests/jungle_temple",               ModItems.BOARD_PISTON,    0.07f, 1, 1);
        addToLoot("chests/desert_pyramid",              ModItems.BOARD_PISTON,    0.07f, 1, 1);

        addToLoot("chests/shipwreck_treasure",          ModItems.BOARD_WOOL,      0.08f, 1, 1);
        addToLoot("chests/pillager_outpost",            ModItems.BOARD_WOOL,      0.07f, 1, 1);

        addToLoot("chests/ancient_city",                ModItems.BOARD_CORRUPTED, 0.02f, 1, 1);
        addToLoot("chests/end_city_treasure",           ModItems.BOARD_CORRUPTED, 0.02f, 1, 1);

        // === CHIPS ===
        addToLoot("chests/simple_dungeon",              ModItems.CHIP_BASIC,      0.15f, 1, 1);
        addToLoot("chests/abandoned_mineshaft",         ModItems.CHIP_BASIC,      0.15f, 1, 1);

        addToLoot("chests/nether_bridge",               ModItems.CHIP_HEART,      0.06f, 1, 1);
        addToLoot("chests/bastion_other",               ModItems.CHIP_HEART,      0.05f, 1, 1);

        addToLoot("chests/stronghold_library",          ModItems.CHIP_DIAMOND,    0.06f, 1, 1);
        addToLoot("chests/woodland_mansion",            ModItems.CHIP_DIAMOND,    0.05f, 1, 1);

        addToLoot("chests/pillager_outpost",            ModItems.CHIP_SPADE,      0.06f, 1, 1);
        addToLoot("chests/desert_pyramid",              ModItems.CHIP_SPADE,      0.05f, 1, 1);

        addToLoot("chests/jungle_temple",               ModItems.CHIP_CLUB,       0.05f, 1, 1);
        addToLoot("chests/buried_treasure",             ModItems.CHIP_CLUB,       0.04f, 1, 1);

        addToLoot("chests/village/village_butcher",     ModItems.CHIP_FOOD,       0.06f, 1, 1);
        addToLoot("chests/village/village_fisher",      ModItems.CHIP_FOOD,       0.06f, 1, 1);

        addToLoot("chests/ruined_portal",            ModItems.CHIP_PORTAL,     0.05f, 1, 1);
        addToLoot("chests/stronghold_corridor",         ModItems.CHIP_PORTAL,     0.04f, 1, 1);

        addToLoot("chests/ancient_city",                ModItems.CHIP_DARK,       0.02f, 1, 1);
        addToLoot("chests/end_city_treasure",           ModItems.CHIP_DARK,       0.02f, 1, 1);
        // === BASIC DISKS ===

        addDiskToLoot("chests/village/village_plains", ModItems.SWIFT_FEET_DISK, 0.10f);
        addDiskToLoot("chests/village/village_savanna", ModItems.SWIFT_FEET_DISK, 0.10f);
        addDiskToLoot("chests/pillager_outpost", ModItems.SWIFT_FEET_DISK, 0.08f);
        addDiskToLoot("chests/simple_dungeon", ModItems.SWIFT_FEET_DISK, 0.12f);
        addDiskToLoot("chests/underwater_ruin_small", ModItems.SEA_FISH_DISK, 0.12f);
        addDiskToLoot("chests/underwater_ruin_big", ModItems.SEA_FISH_DISK, 0.12f);
        addDiskToLoot("chests/shipwreck_treasure", ModItems.SEA_FISH_DISK, 0.10f);
        addDiskToLoot("chests/buried_treasure", ModItems.SEA_FISH_DISK, 0.08f);
        addDiskToLoot("chests/village/village_toolsmith", ModItems.MAGNET_DISK, 0.12f);
        addDiskToLoot("chests/village/village_weaponsmith", ModItems.MAGNET_DISK, 0.12f);
        addDiskToLoot("chests/abandoned_mineshaft", ModItems.MAGNET_DISK, 0.10f);
        addDiskToLoot("chests/stronghold_corridor", ModItems.MAGNET_DISK, 0.08f);
        addDiskToLoot("chests/abandoned_mineshaft", ModItems.MIGHTY_MINER_DISK, 0.12f);
        addDiskToLoot("chests/stronghold_corridor", ModItems.MIGHTY_MINER_DISK, 0.10f);
        addDiskToLoot("chests/simple_dungeon", ModItems.MIGHTY_MINER_DISK, 0.10f);
        addDiskToLoot("chests/village/village_toolsmith", ModItems.MIGHTY_MINER_DISK, 0.08f);
        addDiskToLoot("chests/stronghold_library", ModItems.NIGHT_VISION_DISK, 0.12f);
        addDiskToLoot("chests/ancient_city", ModItems.NIGHT_VISION_DISK, 0.10f);
        addDiskToLoot("chests/simple_dungeon", ModItems.NIGHT_VISION_DISK, 0.10f);
        addDiskToLoot("chests/woodland_mansion", ModItems.NIGHT_VISION_DISK, 0.08f);
        addDiskToLoot("chests/village/village_butcher", ModItems.LIGHTWEIGHT_DISK, 0.12f);
        addDiskToLoot("chests/village/village_fisher", ModItems.LIGHTWEIGHT_DISK, 0.12f);
        addDiskToLoot("chests/shipwreck_supply", ModItems.LIGHTWEIGHT_DISK, 0.10f);
        addDiskToLoot("chests/igloo_chest", ModItems.LIGHTWEIGHT_DISK, 0.08f);
        addDiskToLoot("chests/jungle_temple", ModItems.FEATHER_FALL_DISK, 0.12f);
        addDiskToLoot("chests/end_city_treasure", ModItems.FEATHER_FALL_DISK, 0.10f);
        addDiskToLoot("chests/pillager_outpost", ModItems.FEATHER_FALL_DISK, 0.10f);
        addDiskToLoot("chests/stronghold_crossing", ModItems.FEATHER_FALL_DISK, 0.08f);
        addDiskToLoot("chests/village/village_toolsmith", ModItems.EFFICIENT_DISK, 0.12f);
        addDiskToLoot("chests/village/village_weaponsmith", ModItems.EFFICIENT_DISK, 0.12f);
        addDiskToLoot("chests/abandoned_mineshaft", ModItems.EFFICIENT_DISK, 0.10f);
        addDiskToLoot("chests/stronghold_library", ModItems.EFFICIENT_DISK, 0.08f);
        addDiskToLoot("chests/village/village_toolsmith", ModItems.TANKY_DISK, 0.12f);
        addDiskToLoot("chests/woodland_mansion", ModItems.TANKY_DISK, 0.12f);
        addDiskToLoot("chests/simple_dungeon", ModItems.TANKY_DISK, 0.08f);
        addDiskToLoot("chests/abandoned_mineshaft", ModItems.TANKY_DISK, 0.08f);

        // === RARE DISKS ===

        addDiskToLoot("chests/nether_bridge", ModItems.FLAME_WALKER_DISK, 0.08f);
        addDiskToLoot("chests/ruined_portal", ModItems.FLAME_WALKER_DISK, 0.06f);
        addDiskToLoot("chests/bastion_hoglin_stable", ModItems.FLAME_WALKER_DISK, 0.05f);
        addDiskToLoot("chests/jungle_temple", ModItems.STEP_ASSIST_DISK, 0.07f);
        addDiskToLoot("chests/desert_pyramid", ModItems.STEP_ASSIST_DISK, 0.07f);
        addDiskToLoot("chests/stronghold_library", ModItems.STEP_ASSIST_DISK, 0.06f);
        addDiskToLoot("chests/village/village_shepherd", ModItems.HARVESTER_DISK, 0.08f);
        addDiskToLoot("chests/village/village_fisher", ModItems.HARVESTER_DISK, 0.07f);
        addDiskToLoot("chests/woodland_mansion", ModItems.HARVESTER_DISK, 0.05f);
        addDiskToLoot("chests/village/village_shepherd", ModItems.BEAST_WHISPERER_DISK, 0.08f);
        addDiskToLoot("chests/pillager_outpost", ModItems.BEAST_WHISPERER_DISK, 0.06f);
        addDiskToLoot("chests/simple_dungeon", ModItems.BEAST_WHISPERER_DISK, 0.06f);
        addDiskToLoot("chests/village/village_armorer", ModItems.IRON_GRIP_DISK, 0.08f);
        addDiskToLoot("chests/village/village_weaponsmith", ModItems.IRON_GRIP_DISK, 0.06f);
        addDiskToLoot("chests/desert_pyramid", ModItems.IRON_GRIP_DISK, 0.05f);
        addDiskToLoot("chests/shipwreck_treasure", ModItems.TREASURE_SENSE_DISK, 0.07f);
        addDiskToLoot("chests/bastion_treasure", ModItems.TREASURE_SENSE_DISK, 0.08f);
        addDiskToLoot("chests/desert_pyramid", ModItems.TREASURE_SENSE_DISK, 0.06f);
        addDiskToLoot("chests/village/village_toolsmith", ModItems.PAWNBROKER_DISK, 0.07f);
        addDiskToLoot("chests/woodland_mansion", ModItems.PAWNBROKER_DISK, 0.07f);
        addDiskToLoot("chests/stronghold_library", ModItems.PAWNBROKER_DISK, 0.06f);

        // === EPIC DISKS ===

        addDiskToLoot("chests/bastion_treasure", ModItems.GLUTTON_DISK, 0.04f);
        addDiskToLoot("chests/end_city_treasure", ModItems.GLUTTON_DISK, 0.03f);
        addDiskToLoot("chests/stronghold_library", ModItems.SOAPY_HANDS_DISK, 0.03f);
        addDiskToLoot("chests/ancient_city", ModItems.SOAPY_HANDS_DISK, 0.04f);
        addDiskToLoot("chests/pillager_outpost", ModItems.MOUNTAIN_GOAT_DISK, 0.02f);
        addDiskToLoot("chests/trial_chambers/reward", ModItems.MOUNTAIN_GOAT_DISK, 0.04f);
        addDiskToLoot("chests/shipwreck_treasure", ModItems.WARCHEMIST_DISK, 0.03f);
        addDiskToLoot("chests/trial_chambers/reward", ModItems.WARCHEMIST_DISK, 0.04f);
        addDiskToLoot("chests/village/village_toolsmith", ModItems.BASHER_DISK, 0.03f);
        addDiskToLoot("chests/desert_pyramid", ModItems.BASHER_DISK, 0.03f);

        // === LEGENDARY DISKS ===

        addDiskToLoot("chests/end_city_treasure", ModItems.BERSERKER_DISK, 0.02f);
        addDiskToLoot("chests/ancient_city_ice_box", ModItems.PYROCLASM_DISK, 0.015f);
        addDiskToLoot("chests/shipwreck_map", ModItems.ARCHITECTS_MIND_DISK, 0.02f);
    }

    private static void addDiskToLoot(String lootTable, DeferredHolder<Item, ?> item, float chance) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath("minecraft", lootTable);
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, location);
        LOOT_INJECTIONS.computeIfAbsent(key, k -> new ArrayList<>())
                .add(new LootEntry(item, chance, 1, 1, true, false));
    }

    private static void addFloppyToLoot(String lootTable, DeferredHolder<Item, ?> item, float chance) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath("minecraft", lootTable);
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, location);
        LOOT_INJECTIONS.computeIfAbsent(key, k -> new ArrayList<>())
                .add(new LootEntry(item, chance, 1, 1, false, true));
    }

    private static void addToLoot(String lootTable, DeferredHolder<Item, ?> item, float chance, int minCount, int maxCount) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath("minecraft", lootTable);
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, location);
        LOOT_INJECTIONS.computeIfAbsent(key, k -> new ArrayList<>())
                .add(new LootEntry(item, chance, minCount, maxCount, false, false));
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation tableId = event.getName();
        ResourceKey<LootTable> tableKey = ResourceKey.create(Registries.LOOT_TABLE, tableId);

        List<LootEntry> entries = LOOT_INJECTIONS.get(tableKey);
        if (entries == null) return;

        for (LootEntry entry : entries) {
            LootItem.Builder<?> itemBuilder = LootItem.lootTableItem(entry.item().get())
                    .apply(SetItemCountFunction.setCount(
                            entry.minCount() == entry.maxCount()
                                    ? ConstantValue.exactly(entry.minCount())
                                    : UniformGenerator.between(entry.minCount(), entry.maxCount())));

            if (entry.isDisk()) {
                itemBuilder.apply(() -> LuckyLevelFunction.INSTANCE);
            }
            if (entry.isFloppy()) {
                itemBuilder.apply(() -> FloppyLootFunction.INSTANCE);
            }

            LootPool.Builder poolBuilder = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .add(itemBuilder)
                    .when(LootItemRandomChanceCondition.randomChance(entry.chance()));

            event.getTable().addPool(poolBuilder.build());
        }
    }
}