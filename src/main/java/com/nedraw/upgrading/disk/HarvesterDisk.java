package com.nedraw.upgrading.disk;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class HarvesterDisk extends UpgradeDisk {

    // Track which level is currently applied to each player
    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    private static final Random RANDOM = new Random();

    public HarvesterDisk() {
        super("harvester", "Harvester", DiskRarity.RARE);
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        // Update tracking
        if (appliedLevel == null || appliedLevel != level) {
            APPLIED_LEVELS.put(playerId, level);
        }

        // Crop duplication is handled in BlockBreakHandler event
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        // No continuous effects needed
    }

    @Override
    public void removeEffect(Player player) {
        APPLIED_LEVELS.remove(player.getUUID());
    }

    // Call this from BlockBreakHandler when breaking crops
    public void handleCropBreak(Player player, BlockState state, BlockPos pos, int level) {
        // Server-side only
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        Block block = state.getBlock();
        boolean isValidCrop = false;

        // Check if it's a CropBlock (wheat, carrots, potatoes, beetroots)
        if (block instanceof CropBlock cropBlock) {
            // Only trigger on fully grown crops
            if (cropBlock.isMaxAge(state)) {
                isValidCrop = true;
            }
        }
        // Check for Sweet Berry Bush (not a CropBlock)
        else if (block == Blocks.SWEET_BERRY_BUSH) {
            // Sweet berries are harvestable at age 2 or 3
            int age = state.getValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE);
            if (age >= 2) {
                isValidCrop = true;
            }
        }
        // Check for Nether Wart (not a CropBlock)
        else if (block == Blocks.NETHER_WART) {
            // Nether wart is fully grown at age 3
            int age = state.getValue(net.minecraft.world.level.block.NetherWartBlock.AGE);
            if (age >= 3) {
                isValidCrop = true;
            }
        }

        if (!isValidCrop) return;

        // Calculate duplication chance
        float dupChance;
        if (level < 12) {
            dupChance = (level - 3) * 3 / 100.0f; // 3% per level above start
        } else {
            dupChance = 0.37f; // 37% at level 12
        }

        // Roll for duplication
        if (RANDOM.nextFloat() < dupChance) {
            // Get the crop item that would drop
            ItemStack cropDrop = getCropDrop(block);

            if (!cropDrop.isEmpty()) {
                // Drop duplicate crop
                Block.popResource(serverLevel, pos, cropDrop.copy());
            }
        }

        // Level 12: Bonus items (5% chance)
        if (level >= 12 && RANDOM.nextFloat() < 0.05f) {
            Item bonusItem = getBonusItem(block);
            if (bonusItem != null) {
                ItemStack bonusDrop = new ItemStack(bonusItem);
                Block.popResource(serverLevel, pos, bonusDrop);
            }
        }
    }

    private ItemStack getCropDrop(Block cropBlock) {
        // Return the corresponding crop item
        if (cropBlock == Blocks.WHEAT) {
            return new ItemStack(Items.WHEAT, 1);
        } else if (cropBlock == Blocks.CARROTS) {
            return new ItemStack(Items.CARROT, 1);
        } else if (cropBlock == Blocks.POTATOES) {
            return new ItemStack(Items.POTATO, 1);
        } else if (cropBlock == Blocks.BEETROOTS) {
            return new ItemStack(Items.BEETROOT, 1);
        } else if (cropBlock == Blocks.NETHER_WART) {
            return new ItemStack(Items.NETHER_WART, 1);
        } else if (cropBlock == Blocks.SWEET_BERRY_BUSH) {
            return new ItemStack(Items.SWEET_BERRIES, 1);
        }

        return ItemStack.EMPTY;
    }

    private Item getBonusItem(Block cropBlock) {
        // Random bonus item selection
        int roll = RANDOM.nextInt(100);

        if (roll < 50) {
            // 50% - Bone Meal (Common)
            return Items.BONE_MEAL;
        } else if (roll < 80) {
            // 30% - Another copy of the crop (Uncommon)
            return getCropItemType(cropBlock);
        } else {
            // 20% - Golden version of the crop (Rare)
            return getGoldenCropVariant(cropBlock);
        }
    }

    private Item getCropItemType(Block cropBlock) {
        // Return the crop item type
        if (cropBlock == Blocks.WHEAT) return Items.WHEAT;
        if (cropBlock == Blocks.CARROTS) return Items.CARROT;
        if (cropBlock == Blocks.POTATOES) return Items.POTATO;
        if (cropBlock == Blocks.BEETROOTS) return Items.BEETROOT;
        if (cropBlock == Blocks.NETHER_WART) return Items.NETHER_WART;
        if (cropBlock == Blocks.SWEET_BERRY_BUSH) return Items.SWEET_BERRIES;
        return Items.WHEAT; // Fallback
    }

    private Item getGoldenCropVariant(Block cropBlock) {
        // Return custom golden crop variants
        if (cropBlock == Blocks.WHEAT) {
            return com.nedraw.upgrading.item.ModItems.GOLDEN_WHEAT.get();
        } else if (cropBlock == Blocks.CARROTS) {
            return Items.GOLDEN_CARROT; // Vanilla golden carrot
        } else if (cropBlock == Blocks.POTATOES) {
            return com.nedraw.upgrading.item.ModItems.GOLDEN_POTATO.get();
        } else if (cropBlock == Blocks.BEETROOTS) {
            return com.nedraw.upgrading.item.ModItems.GOLDEN_BEETROOT.get();
        } else if (cropBlock == Blocks.NETHER_WART) {
            return com.nedraw.upgrading.item.ModItems.GOLDEN_NETHER_WART.get();
        } else if (cropBlock == Blocks.SWEET_BERRY_BUSH) {
            return com.nedraw.upgrading.item.ModItems.GOLDEN_SWEET_BERRIES.get();
        }

        return Items.GOLDEN_CARROT; // Fallback
    }

    public int getAppliedLevel(UUID playerId) {
        return APPLIED_LEVELS.getOrDefault(playerId, 0);
    }
}