package com.nedraw.upgrading.disk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static java.lang.Math.round;

public class MightyMinerDisk extends UpgradeDisk {
    private static final ResourceLocation MINING_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "mighty_miner_efficiency");

    // Track which level is currently applied to each player
    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    private static final Random RANDOM = new Random();

    public MightyMinerDisk() {
        super("mighty_miner", "Mighty Miner", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        // Only update attributes if level changed
        if (appliedLevel == null || appliedLevel != level) {
            var miningAttribute = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);

            if (miningAttribute != null) {
                // Remove old modifier
                miningAttribute.removeModifier(MINING_SPEED_MODIFIER_ID);

                // Calculate mining speed bonus
                double miningBonus;
                miningBonus = 2 + round(Math.pow(level, 2.8) /20) / 100.0;

                // Add new modifier
                AttributeModifier miningModifier = new AttributeModifier(
                        MINING_SPEED_MODIFIER_ID,
                        miningBonus,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                miningAttribute.addPermanentModifier(miningModifier);
            }

            APPLIED_LEVELS.put(playerId, level);
        }
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        // No continuous effects needed for mining speed
        // Ore finding is handled in BlockBreakHandler event
    }

    @Override
    public void removeEffect(Player player) {
        var miningAttribute = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);

        if (miningAttribute != null) {
            miningAttribute.removeModifier(MINING_SPEED_MODIFIER_ID);
        }

        APPLIED_LEVELS.remove(player.getUUID());
    }

    // Handle ore finding when breaking stone (called from BlockBreakHandler)
    public void handleBlockBreak(Player player, BlockState state, BlockPos pos, int level) {
        // Server-side only
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        // Level 12: Chance to find ores when breaking stone
        if (level >= 12) {
            Block block = state.getBlock();

            // Check if breaking stone, deepslate, or similar
            if (block == Blocks.STONE || block == Blocks.DEEPSLATE ||
                    block == Blocks.COBBLESTONE || block == Blocks.COBBLED_DEEPSLATE) {

                // 6% chance to drop a random ore
                if (RANDOM.nextDouble() < 0.06) {
                    // Weighted ore selection
                    int roll = RANDOM.nextInt(100);
                    Item selectedOre;

                    if (roll < 30) {
                        selectedOre = Items.COAL; // 30%
                    } else if (roll < 55) {
                        selectedOre = Items.RAW_IRON; // 25%
                    } else if (roll < 75) {
                        selectedOre = Items.RAW_COPPER; // 20%
                    } else if (roll < 85) {
                        selectedOre = Items.RAW_GOLD; // 10%
                    } else if (roll < 92) {
                        selectedOre = Items.REDSTONE; // 7%
                    } else if (roll < 96) {
                        selectedOre = Items.LAPIS_LAZULI; // 4%
                    } else if (roll < 99) {
                        selectedOre = Items.EMERALD; // 3%
                    } else {
                        selectedOre = Items.DIAMOND; // 1%
                    }

                    // Drop the ore block
                    ItemStack oreDrop = new ItemStack(selectedOre);
                    Block.popResource(serverLevel, pos, oreDrop);
                }
            }
        }
    }
}