package com.nedraw.upgrading.loot;

import com.mojang.serialization.MapCodec;
import com.nedraw.upgrading.disk.DiskRarity;
import com.nedraw.upgrading.item.EncryptedFloppyItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Random;

public class FloppyLootFunction extends LootItemConditionalFunction {

    private static final Random RANDOM = new Random();

    public static final FloppyLootFunction INSTANCE = new FloppyLootFunction(List.of());
    public static final MapCodec<FloppyLootFunction> CODEC = MapCodec.unit(INSTANCE);

    protected FloppyLootFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        DiskRarity startingRarity = rollStartingRarity();
        int amountOfChances = rollAmountOfChances(startingRarity);
        EncryptedFloppyItem.setComponents(stack, startingRarity, amountOfChances);
        return stack;
    }

    @Override
    public LootItemFunctionType<FloppyLootFunction> getType() {
        return ModLootFunctions.FLOPPY_LOOT.get();
    }

    private static DiskRarity rollStartingRarity() {
        float roll = RANDOM.nextFloat();
        if (roll < 0.02f) return DiskRarity.EPIC;
        if (roll < 0.10f) return DiskRarity.RARE;
        return DiskRarity.BASIC;
    }

    private static int rollAmountOfChances(DiskRarity rarity) {
        return switch (rarity) {
            case BASIC -> {
                float roll = RANDOM.nextFloat();
                if (roll < 0.15f) yield 3;
                if (roll < 0.30f) yield 5;
                yield 4;
            }
            case RARE -> {
                float roll = RANDOM.nextFloat();
                if (roll < 0.15f) yield 2;
                if (roll < 0.30f) yield 4;
                yield 3;
            }
            case EPIC -> {
                float roll = RANDOM.nextFloat();
                if (roll < 0.15f) yield 1;
                if (roll < 0.30f) yield 3;
                yield 2;
            }
            default -> 4;
        };
    }
}