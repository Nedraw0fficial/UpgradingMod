package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class BlockOEnchantingDisk extends UpgradeDisk {

    private static final Random RANDOM = new Random();
    private static final Map<UUID, Integer> XP_COLLECTED = new HashMap<>();

    private static final float[] CHANCES = {0, 0, 0, 0.02f, 0.025f, 0.035f, 0.04f, 0.05f, 0.055f, 0.065f, 0.073f, 0.073f};
    private static final int[]   XP_AMOUNTS = {0, 0, 0, 5, 6, 8, 10, 12, 14, 16, 20, 20};

    public BlockOEnchantingDisk() {
        super("block_o_enchanting", "Block O' Enchanting", DiskRarity.RARE);
    }

    public void handleBlockBreak(Player player, boolean blockDropsXP, int level, float efficiency) {
        float chance = CHANCES[Math.min(level, CHANCES.length - 1)] * efficiency;
        if (RANDOM.nextFloat() >= chance) return;

        int xpAmount = (int)(XP_AMOUNTS[Math.min(level, XP_AMOUNTS.length - 1)] * efficiency);

        if (level >= 12 && blockDropsXP) xpAmount *= 2;

        net.minecraft.world.entity.ExperienceOrb.award(
                (net.minecraft.server.level.ServerLevel) player.level(),
                player.position(),
                xpAmount
        );

        UUID playerId = player.getUUID();
        int total = XP_COLLECTED.getOrDefault(playerId, 0) + xpAmount;
        XP_COLLECTED.put(playerId, total);

        if (total >= 3500 && player instanceof ServerPlayer sp) {
            ModAdvancementTriggers.XP_LEAKS(sp);
        }
    }
}