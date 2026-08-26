package com.nedraw.upgrading.mixin;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRarity;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public class BowItemMixin {

    @Inject(
            method = "getUseDuration(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)I",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyDrawSpeed(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        if (!(entity instanceof Player player)) return;

        boolean isClient = entity.level().isClientSide;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId == null) continue;

            UpgradeDisk disk = DiskRegistry.getDisk(diskId);
            if (disk == null) continue;
            if (disk.getRarity() != DiskRarity.MYTHIC) continue;
            if (!diskId.equals("necro_archer")) continue;

            int level = diskData.getDiskLevel(diskId);
            float multiplier = getSpeedMultiplier(player.getUUID(), level);

            if (multiplier != 1.0f) {
                int newDuration = Math.round(cir.getReturnValue() / multiplier);
                cir.setReturnValue(Math.max(1, newDuration));
            }
            return;
        }
    }

    private float getSpeedMultiplier(java.util.UUID playerId, int level) {
        boolean inBoost = com.nedraw.upgrading.NecroArcherHandler.isPlayerBoosted(playerId);

        if (inBoost) {
            return level >= 12 ? 100.0f : 1.8f;
        } else {
            return level >= 12 ? 1.8f : 1.6f;
        }
    }
}