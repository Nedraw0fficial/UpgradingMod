package com.nedraw.upgrading;

import com.nedraw.upgrading.data.PlayerDiskData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid = UpgradingMod.MODID)
public class LightweightHandler {

    @SubscribeEvent
    public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Check if item has food properties
        var foodProperties = event.getItem().getFoodProperties(player);
        if (foodProperties == null) return;

        // Check if player has Lightweight equipped at level 12
        PlayerDiskData diskData = PlayerDiskData.get(player);

        for (int slot = 0; slot < 3; slot++) {
            String diskId = diskData.getEquippedDisk(slot);
            if (diskId != null && diskId.equals("lightweight")) {
                int level = diskData.getDiskLevel(diskId);

                if (level >= 12) {
                    // Add 15% bonus saturation
                    FoodData foodData = player.getFoodData();

                    float baseSaturation = foodProperties.saturation();
                    float bonusSaturation = baseSaturation * 0.15f;

                    // Add bonus saturation
                    foodData.setSaturation(Math.min(20.0f, foodData.getSaturationLevel() + bonusSaturation));
                }

                return; // Only apply once
            }
        }
    }
}