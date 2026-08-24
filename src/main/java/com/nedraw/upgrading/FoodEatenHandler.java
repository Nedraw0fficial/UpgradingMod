package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import com.nedraw.upgrading.disk.DiskRegistry;
import com.nedraw.upgrading.disk.GluttonDisk;
import com.nedraw.upgrading.disk.UpgradeDisk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class FoodEatenHandler {

    @SubscribeEvent
    public static void onFoodFinish(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (player.level().isClientSide) return;

        ItemStack stack = event.getItem();

        if (!stack.has(net.minecraft.core.component.DataComponents.FOOD)) return;

        PlayerDiskData diskData = PlayerDiskData.get(player);

        // Check all equipped disks for Glutton
        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if ("glutton".equals(diskId)) {
                UpgradeDisk disk = DiskRegistry.getDisk(diskId);
                if (disk instanceof GluttonDisk glutton) {
                    int level = diskData.getDiskLevel(diskId);

                    // Get food properties from DataComponents
                    var foodComponent = stack.get(net.minecraft.core.component.DataComponents.FOOD);
                    if (foodComponent != null) {
                        int nutrition = foodComponent.nutrition();
                        float saturation = foodComponent.saturation();

                        // Handle food eaten - this now handles EVERYTHING including vanilla behavior
                        glutton.handleFoodEaten(player, nutrition, saturation, level);
                    }
                }
                break; // Only one Glutton can be equipped
            }
        }
    }
}