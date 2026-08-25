package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();
    private static final Random RANDOM = new Random();

    public HarvesterDisk() {
        super("harvester", "Harvester", DiskRarity.RARE);
    }

    @Override
    public void applyEffect(Player player, int level) {
        APPLIED_LEVELS.put(player.getUUID(), level);
    }

    @Override
    public void removeEffect(Player player) {
        APPLIED_LEVELS.remove(player.getUUID());
    }

    public void handleCropBreak(Player player, BlockState state, BlockPos pos, int level, float efficiency) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        Block block = state.getBlock();
        boolean isValidCrop = false;

        if (block instanceof CropBlock cropBlock && cropBlock.isMaxAge(state)) {
            isValidCrop = true;
        } else if (block == Blocks.SWEET_BERRY_BUSH) {
            int age = state.getValue(net.minecraft.world.level.block.SweetBerryBushBlock.AGE);
            if (age >= 2) isValidCrop = true;
        } else if (block == Blocks.NETHER_WART) {
            int age = state.getValue(net.minecraft.world.level.block.NetherWartBlock.AGE);
            if (age >= 3) isValidCrop = true;
        }

        if (!isValidCrop) return;

        float dupChance = (level < 12 ? (level - 3) * 3 / 100.0f : 0.37f) * efficiency;

        if (RANDOM.nextFloat() < dupChance) {
            ItemStack cropDrop = getCropDrop(block);
            if (!cropDrop.isEmpty()) Block.popResource(serverLevel, pos, cropDrop.copy());
        }

        if (level >= 12 && RANDOM.nextFloat() < 0.05f * efficiency) {
            Item bonusItem = getBonusItem(block, player, serverLevel, pos);
            if (bonusItem != null) Block.popResource(serverLevel, pos, new ItemStack(bonusItem));
        }
    }

    // Keep old signature for backwards compat
    public void handleCropBreak(Player player, BlockState state, BlockPos pos, int level) {
        handleCropBreak(player, state, pos, level, 1.0f);
    }

    private Item getBonusItem(Block cropBlock, Player player, ServerLevel serverLevel, BlockPos pos) {
        int roll = RANDOM.nextInt(100);
        if (roll < 50) return Items.BONE_MEAL;
        else if (roll < 80) return getCropItemType(cropBlock);
        else {
            if (player instanceof ServerPlayer sp) ModAdvancementTriggers.GOLDEN_CROP_FOUND(sp);
            return getGoldenCropVariant(cropBlock);
        }
    }

    private ItemStack getCropDrop(Block cropBlock) {
        if (cropBlock == Blocks.WHEAT)             return new ItemStack(Items.WHEAT, 1);
        if (cropBlock == Blocks.CARROTS)           return new ItemStack(Items.CARROT, 1);
        if (cropBlock == Blocks.POTATOES)          return new ItemStack(Items.POTATO, 1);
        if (cropBlock == Blocks.BEETROOTS)         return new ItemStack(Items.BEETROOT, 1);
        if (cropBlock == Blocks.NETHER_WART)       return new ItemStack(Items.NETHER_WART, 1);
        if (cropBlock == Blocks.SWEET_BERRY_BUSH)  return new ItemStack(Items.SWEET_BERRIES, 1);
        return ItemStack.EMPTY;
    }

    private Item getCropItemType(Block cropBlock) {
        if (cropBlock == Blocks.WHEAT)            return Items.WHEAT;
        if (cropBlock == Blocks.CARROTS)          return Items.CARROT;
        if (cropBlock == Blocks.POTATOES)         return Items.POTATO;
        if (cropBlock == Blocks.BEETROOTS)        return Items.BEETROOT;
        if (cropBlock == Blocks.NETHER_WART)      return Items.NETHER_WART;
        if (cropBlock == Blocks.SWEET_BERRY_BUSH) return Items.SWEET_BERRIES;
        return Items.WHEAT;
    }

    private Item getGoldenCropVariant(Block cropBlock) {
        if (cropBlock == Blocks.WHEAT)            return com.nedraw.upgrading.item.ModItems.GOLDEN_WHEAT.get();
        if (cropBlock == Blocks.CARROTS)          return Items.GOLDEN_CARROT;
        if (cropBlock == Blocks.POTATOES)         return com.nedraw.upgrading.item.ModItems.GOLDEN_POTATO.get();
        if (cropBlock == Blocks.BEETROOTS)        return com.nedraw.upgrading.item.ModItems.GOLDEN_BEETROOT.get();
        if (cropBlock == Blocks.NETHER_WART)      return com.nedraw.upgrading.item.ModItems.GOLDEN_NETHER_WART.get();
        if (cropBlock == Blocks.SWEET_BERRY_BUSH) return com.nedraw.upgrading.item.ModItems.GOLDEN_SWEET_BERRIES.get();
        return Items.GOLDEN_CARROT;
    }

    public int getAppliedLevel(UUID playerId) {
        return APPLIED_LEVELS.getOrDefault(playerId, 0);
    }
}
