package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class MightyMinerDisk extends UpgradeDisk {
    private static final ResourceLocation MINING_SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "mighty_miner_efficiency");
    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();
    private static final Random RANDOM = new Random();

    public MightyMinerDisk() {
        super("mighty_miner", "Mighty Miner", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level, int slot, float efficiency) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        if (appliedLevel == null || appliedLevel != level) {
            var miningAttribute = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
            if (miningAttribute != null) {
                miningAttribute.removeModifier(MINING_SPEED_MODIFIER_ID);
                double miningBonus = getMiningBonus(level) * efficiency;
                miningAttribute.addPermanentModifier(new AttributeModifier(
                        MINING_SPEED_MODIFIER_ID, miningBonus,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
            APPLIED_LEVELS.put(playerId, level);
        }
    }

    @Override
    public void removeEffect(Player player) {
        var miningAttribute = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        if (miningAttribute != null) miningAttribute.removeModifier(MINING_SPEED_MODIFIER_ID);
        APPLIED_LEVELS.remove(player.getUUID());
    }

    private double getMiningBonus(int level) {
        return switch (level) {
            case 1  -> 0.02; case 2  -> 0.02; case 3  -> 0.03;
            case 4  -> 0.04; case 5  -> 0.07; case 6  -> 0.10;
            case 7  -> 0.14; case 8  -> 0.19; case 9  -> 0.25;
            case 10 -> 0.34; case 11 -> 0.43; case 12 -> 0.55;
            default -> 0.02;
        };
    }

    public void handleBlockBreak(Player player, BlockState state, BlockPos pos, int level) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;
        if (level >= 12) {
            Block block = state.getBlock();
            if (block == Blocks.STONE || block == Blocks.DEEPSLATE ||
                    block == Blocks.COBBLESTONE || block == Blocks.COBBLED_DEEPSLATE) {
                if (RANDOM.nextDouble() < 0.01) {
                    int roll = RANDOM.nextInt(100);
                    Item selectedOre;
                    if (roll < 30)      selectedOre = Items.COAL;
                    else if (roll < 55) selectedOre = Items.RAW_IRON;
                    else if (roll < 75) selectedOre = Items.RAW_COPPER;
                    else if (roll < 85) selectedOre = Items.RAW_GOLD;
                    else if (roll < 92) selectedOre = Items.REDSTONE;
                    else if (roll < 96) selectedOre = Items.LAPIS_LAZULI;
                    else if (roll < 99) selectedOre = Items.EMERALD;
                    else                selectedOre = Items.DIAMOND;
                    Block.popResource(serverLevel, pos, new ItemStack(selectedOre));
                    if (player instanceof ServerPlayer sp) ModAdvancementTriggers.BONUS_ORE_FOUND(sp);
                }
            }
        }
    }
}
