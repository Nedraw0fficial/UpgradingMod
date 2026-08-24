package com.nedraw.upgrading.loot;

import com.nedraw.upgrading.UpgradingMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModLootFunctions {

    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, UpgradingMod.MODID);

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<LuckyLevelFunction>> LUCKY_LEVEL =
            LOOT_FUNCTIONS.register("lucky_level",
                    () -> new LootItemFunctionType<>(LuckyLevelFunction.CODEC));

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<FloppyLootFunction>> FLOPPY_LOOT =
            LOOT_FUNCTIONS.register("floppy_loot",
                    () -> new LootItemFunctionType<>(FloppyLootFunction.CODEC));
}