package com.nedraw.upgrading.client;

import com.nedraw.upgrading.NecroArcherHandler;
import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRarity;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

/**
 * Overrides the vanilla "pull" item property for bows so that Necro-Archer's
 * draw speed bonus is reflected VISUALLY in the bow pull animation.
 *
 * Vanilla formula (hardcoded /20.0F): pull = (useDuration - remainingTicks) / 20.0F
 * Our formula: pull = (usedTicks * speedMultiplier) / 20.0F, clamped to 1.0
 */
public class NecroArcherBowProperties {

    public static void register() {
        ItemProperties.register(
                Items.BOW,
                ResourceLocation.withDefaultNamespace("pull"),
                (ClampedItemPropertyFunction) (stack, level, entity, seed) -> {
                    if (entity == null) return 0.0F;
                    if (entity.getUseItem() != stack) return 0.0F;

                    int useDuration = stack.getItem().getUseDuration(stack, entity);
                    int remaining = entity.getUseItemRemainingTicks();
                    int usedTicks = useDuration - remaining;

                    float speedMultiplier = getVisualSpeedMultiplier(entity);

                    float pull = (usedTicks * speedMultiplier) / 20.0F;
                    return Math.min(pull, 1.0F);
                }
        );
    }

    private static float getVisualSpeedMultiplier(LivingEntity entity) {
        if (!(entity instanceof Player player)) return 1.0F;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId == null) continue;

            UpgradeDisk disk = DiskRegistry.getDisk(diskId);
            if (disk == null) continue;
            if (disk.getRarity() != DiskRarity.MYTHIC) continue;
            if (!diskId.equals("necro_archer")) continue;

            int level = diskData.getDiskLevel(diskId);
            boolean inBoost = NecroArcherHandler.isPlayerBoosted(player.getUUID());

            if (inBoost && level >= 12) {
                return 100.0F;
            } else if (inBoost) {
                return 1.8F;
            } else if (level >= 12) {
                return 1.8F;
            } else {
                return 1.6F;
            }
        }

        return 1.0F;
    }
}