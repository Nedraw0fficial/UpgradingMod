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
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class DiskLootInjector {

    // Format: Map<LootTableKey, Map<DiskItem, DropChance>>
    private static final Map<ResourceKey<LootTable>, Map<DeferredHolder<Item, ?>, Float>> LOOT_INJECTIONS = new HashMap<>();

    static {

        // === OVERWORLD STRUCTURES ===

        // Villages (all types)
        addToLoot("chests/village/village_plains_house", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_desert_house", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_savanna_house", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_taiga_house", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_snowy_house", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_armorer", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_weaponsmith", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_toolsmith", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_cartographer", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_mason", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_shepherd", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_butcher", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_fletcher", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_fisher", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_tannery", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/village/village_temple", ModItems.ENCRYPTED_FLOPPY, 0.15f);

        // Dungeons & Mines
        addToLoot("chests/simple_dungeon", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/abandoned_mineshaft", ModItems.ENCRYPTED_FLOPPY, 0.15f);

        // Stronghold
        addToLoot("chests/stronghold_corridor", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/stronghold_crossing", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/stronghold_library", ModItems.ENCRYPTED_FLOPPY, 0.15f);

        // Pyramids & Temples
        addToLoot("chests/desert_pyramid", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/jungle_temple", ModItems.ENCRYPTED_FLOPPY, 0.15f);

        // Igloo
        addToLoot("chests/igloo_chest", ModItems.ENCRYPTED_FLOPPY, 0.15f);

        // Pillager Outpost
        addToLoot("chests/pillager_outpost", ModItems.ENCRYPTED_FLOPPY, 0.15f);

        // Woodland Mansion
        addToLoot("chests/woodland_mansion", ModItems.ENCRYPTED_FLOPPY, 0.15f);

        // Shipwreck
        addToLoot("chests/shipwreck_treasure", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/shipwreck_supply", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/shipwreck_map", ModItems.ENCRYPTED_FLOPPY, 0.15f);

        // Buried Treasure
        addToLoot("chests/buried_treasure", ModItems.ENCRYPTED_FLOPPY, 0.20f); // Slightly higher!

        // Ocean Ruins
        addToLoot("chests/underwater_ruin_small", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/underwater_ruin_big", ModItems.ENCRYPTED_FLOPPY, 0.15f);

        // === NETHER ===

        // Nether Fortress
        addToLoot("chests/nether_bridge", ModItems.ENCRYPTED_FLOPPY, 0.15f);

        // Bastion Remnant
        addToLoot("chests/bastion_treasure", ModItems.ENCRYPTED_FLOPPY, 0.20f);
        addToLoot("chests/bastion_other", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/bastion_bridge", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/bastion_hoglin_stable", ModItems.ENCRYPTED_FLOPPY, 0.15f);

        // === THE END ===

        // End City
        addToLoot("chests/end_city_treasure", ModItems.ENCRYPTED_FLOPPY, 0.20f);

        // === ANCIENT CITY (1.19+) ===

        addToLoot("chests/ancient_city", ModItems.ENCRYPTED_FLOPPY, 0.20f);
        addToLoot("chests/ancient_city_ice_box", ModItems.ENCRYPTED_FLOPPY, 0.20f);

        // === TRIAL CHAMBERS (1.21+) ===

        addToLoot("chests/trial_chambers/reward", ModItems.ENCRYPTED_FLOPPY, 0.20f);
        addToLoot("chests/trial_chambers/reward_unique", ModItems.ENCRYPTED_FLOPPY, 0.20f);
        addToLoot("chests/trial_chambers/supply", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/trial_chambers/corridor", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/trial_chambers/entrance", ModItems.ENCRYPTED_FLOPPY, 0.15f);
        addToLoot("chests/trial_chambers/intersection", ModItems.ENCRYPTED_FLOPPY, 0.15f);



        // ===== BASIC DISKS (4 loot tables each, 8-12% chance) =====

        // Swift Feet - Movement/travel themed
        addToLoot("chests/village/village_plains", ModItems.SWIFT_FEET_DISK, 0.10f);
        addToLoot("chests/village/village_savanna", ModItems.SWIFT_FEET_DISK, 0.10f);
        addToLoot("chests/pillager_outpost", ModItems.SWIFT_FEET_DISK, 0.08f);
        addToLoot("chests/simple_dungeon", ModItems.SWIFT_FEET_DISK, 0.12f);

        // Sea Fish - Ocean/water themed
        addToLoot("chests/underwater_ruin_small", ModItems.SEA_FISH_DISK, 0.12f);
        addToLoot("chests/underwater_ruin_big", ModItems.SEA_FISH_DISK, 0.12f);
        addToLoot("chests/shipwreck_treasure", ModItems.SEA_FISH_DISK, 0.10f);
        addToLoot("chests/buried_treasure", ModItems.SEA_FISH_DISK, 0.08f);

        // Magnet - Resource gathering themed
        addToLoot("chests/village/village_toolsmith", ModItems.MAGNET_DISK, 0.12f);
        addToLoot("chests/village/village_weaponsmith", ModItems.MAGNET_DISK, 0.12f);
        addToLoot("chests/abandoned_mineshaft", ModItems.MAGNET_DISK, 0.10f);
        addToLoot("chests/stronghold_corridor", ModItems.MAGNET_DISK, 0.08f);

        // Mighty Miner - Mining/underground themed
        addToLoot("chests/abandoned_mineshaft", ModItems.MIGHTY_MINER_DISK, 0.12f);
        addToLoot("chests/stronghold_corridor", ModItems.MIGHTY_MINER_DISK, 0.10f);
        addToLoot("chests/simple_dungeon", ModItems.MIGHTY_MINER_DISK, 0.10f);
        addToLoot("chests/village/village_toolsmith", ModItems.MIGHTY_MINER_DISK, 0.08f);

        // Night Vision - Visibility/darkness themed
        addToLoot("chests/stronghold_library", ModItems.NIGHT_VISION_DISK, 0.12f);
        addToLoot("chests/ancient_city", ModItems.NIGHT_VISION_DISK, 0.10f);
        addToLoot("chests/simple_dungeon", ModItems.NIGHT_VISION_DISK, 0.10f);
        addToLoot("chests/woodland_mansion", ModItems.NIGHT_VISION_DISK, 0.08f);

        // Lightweight - Resource/survival themed
        addToLoot("chests/village/village_butcher", ModItems.LIGHTWEIGHT_DISK, 0.12f);
        addToLoot("chests/village/village_fisher", ModItems.LIGHTWEIGHT_DISK, 0.12f);
        addToLoot("chests/shipwreck_supply", ModItems.LIGHTWEIGHT_DISK, 0.10f);
        addToLoot("chests/igloo_chest", ModItems.LIGHTWEIGHT_DISK, 0.08f);

        // Feather Fall - Height/climbing themed
        addToLoot("chests/jungle_temple", ModItems.FEATHER_FALL_DISK, 0.12f);
        addToLoot("chests/end_city_treasure", ModItems.FEATHER_FALL_DISK, 0.10f);
        addToLoot("chests/pillager_outpost", ModItems.FEATHER_FALL_DISK, 0.10f);
        addToLoot("chests/stronghold_crossing", ModItems.FEATHER_FALL_DISK, 0.08f);

        // Efficient - Mining/Tools themed
        addToLoot("chests/village/village_toolsmith", ModItems.EFFICIENT_DISK, 0.12f);
        addToLoot("chests/village/village_weaponsmith", ModItems.EFFICIENT_DISK, 0.12f);
        addToLoot("chests/abandoned_mineshaft", ModItems.EFFICIENT_DISK, 0.10f);
        addToLoot("chests/stronghold_library", ModItems.EFFICIENT_DISK, 0.08f);

        // Tanky - Health/Armour themed
        addToLoot("chests/village/village_toolsmith", ModItems.TANKY_DISK, 0.12f);
        addToLoot("chests/woodland_mansion", ModItems.TANKY_DISK, 0.12f);
        addToLoot("chests/simple_dungeon", ModItems.TANKY_DISK, 0.08f);
        addToLoot("chests/abandoned_mineshaft", ModItems.TANKY_DISK, 0.08f);

        // ===== RARE DISKS (3 loot tables each, 5-8% chance) =====

        // Flame Walker - Nether themed
        addToLoot("chests/nether_bridge", ModItems.FLAME_WALKER_DISK, 0.08f);
        addToLoot("chests/bastion_treasure", ModItems.FLAME_WALKER_DISK, 0.06f);
        addToLoot("chests/bastion_hoglin_stable", ModItems.FLAME_WALKER_DISK, 0.05f);

        // Step Assist - Adventure/exploration themed
        addToLoot("chests/jungle_temple", ModItems.STEP_ASSIST_DISK, 0.07f);
        addToLoot("chests/desert_pyramid", ModItems.STEP_ASSIST_DISK, 0.07f);
        addToLoot("chests/stronghold_library", ModItems.STEP_ASSIST_DISK, 0.06f);

        // Harvester - Farming themed
        addToLoot("chests/village/village_shepherd", ModItems.HARVESTER_DISK, 0.08f);
        addToLoot("chests/village/village_fisher", ModItems.HARVESTER_DISK, 0.07f);
        addToLoot("chests/woodland_mansion", ModItems.HARVESTER_DISK, 0.05f);

        // ===== EPIC DISKS (2 loot tables each, 2-4% chance) =====

        // Glutton - Food/treasure themed
        addToLoot("chests/bastion_treasure", ModItems.GLUTTON_DISK, 0.04f);
        addToLoot("chests/end_city_treasure", ModItems.GLUTTON_DISK, 0.03f);

        // Soapy Hands - Combat/stronghold themed
        addToLoot("chests/stronghold_library", ModItems.SOAPY_HANDS_DISK, 0.03f);
        addToLoot("chests/ancient_city", ModItems.SOAPY_HANDS_DISK, 0.04f);

        // ===== LEGENDARY DISKS (1 loot table each, 1-2% chance) =====

        // Berserker - End cities only (combat mastery)
        addToLoot("chests/end_city_treasure", ModItems.BERSERKER_DISK, 0.02f);

        // Pyroclasm - Ancient city ice box only (most dangerous place)
        addToLoot("chests/ancient_city_ice_box", ModItems.PYROCLASM_DISK, 0.015f);
    }

    private static void addToLoot(String lootTable, DeferredHolder<Item, ?> diskItem, float chance) {
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath("minecraft", lootTable);
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, location);
        LOOT_INJECTIONS.computeIfAbsent(key, k -> new HashMap<>()).put(diskItem, chance);
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation tableId = event.getName();
        ResourceKey<LootTable> tableKey = ResourceKey.create(Registries.LOOT_TABLE, tableId);

        Map<DeferredHolder<Item, ?>, Float> injectionsForTable = LOOT_INJECTIONS.get(tableKey);

        if (injectionsForTable != null) {
            for (Map.Entry<DeferredHolder<Item, ?>, Float> entry : injectionsForTable.entrySet()) {
                Item diskItem = entry.getKey().get();
                float dropChance = entry.getValue();

                LootPool.Builder poolBuilder = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(diskItem))
                        .when(LootItemRandomChanceCondition.randomChance(dropChance));

                event.getTable().addPool(poolBuilder.build());
            }
        }
    }
}