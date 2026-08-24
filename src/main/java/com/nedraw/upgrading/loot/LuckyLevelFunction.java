package com.nedraw.upgrading.loot;

import com.mojang.serialization.MapCodec;
import com.nedraw.upgrading.item.DiskItem;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.DiskRarity;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Random;

public class LuckyLevelFunction extends LootItemConditionalFunction {

    private static final Random RANDOM = new Random();

    private static final float BASIC_LUCKY_CHANCE     = 0.15f;
    private static final float RARE_LUCKY_CHANCE      = 0.10f;
    private static final float EPIC_LUCKY_CHANCE      = 0.08f;
    private static final float LEGENDARY_LUCKY_CHANCE = 0.05f;

    public static final LuckyLevelFunction INSTANCE = new LuckyLevelFunction(List.of());

    public static final MapCodec<LuckyLevelFunction> CODEC = MapCodec.unit(INSTANCE);

    protected LuckyLevelFunction(List<LootItemCondition> conditions) {
        super(conditions);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (!(stack.getItem() instanceof DiskItem diskItem)) return stack;

        String diskId = diskItem.getDiskId();
        UpgradeDisk disk = DiskRegistry.getDisk(diskId);
        if (disk == null) return stack;

        DiskRarity rarity = disk.getRarity();
        float luckyChance = getLuckyChance(rarity);

        if (RANDOM.nextFloat() < luckyChance) {
            int luckyLevel = getLuckyLevel(rarity);
            DiskItem.setXpLvl(stack, luckyLevel);
        }

        return stack;
    }

    @Override
    public LootItemFunctionType<LuckyLevelFunction> getType() {
        return ModLootFunctions.LUCKY_LEVEL.get();
    }

    private static float getLuckyChance(DiskRarity rarity) {
        return switch (rarity) {
            case BASIC     -> BASIC_LUCKY_CHANCE;
            case RARE      -> RARE_LUCKY_CHANCE;
            case EPIC      -> EPIC_LUCKY_CHANCE;
            case LEGENDARY -> LEGENDARY_LUCKY_CHANCE;
            case MYTHIC    -> 0f;
        };
    }

    private static int getLuckyLevel(DiskRarity rarity) {
        return switch (rarity) {
            case BASIC     -> 2 + RANDOM.nextInt(4);
            case RARE      -> 5 + RANDOM.nextInt(3);
            case EPIC      -> 8 + RANDOM.nextInt(2);
            case LEGENDARY -> 10;
            case MYTHIC    -> 11;
        };
    }
}