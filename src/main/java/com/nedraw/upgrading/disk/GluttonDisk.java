package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GluttonDisk extends UpgradeDisk {

    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    // Track saturation AND food BEFORE eating
    private static final Map<UUID, Float> SATURATION_BEFORE_EATING = new HashMap<>();
    private static final Map<UUID, Integer> FOOD_BEFORE_EATING = new HashMap<>();

    private static final ResourceLocation MAX_ABSORPTION_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "glutton_max_absorption");

    public GluttonDisk() {
        super("glutton", "Glutton", DiskRarity.EPIC);
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        if (appliedLevel == null || appliedLevel != level) {
            APPLIED_LEVELS.put(playerId, level);

            // Level 12: Add max absorption attribute
            if (level >= 12) {
                var maxAbsorptionAttr = player.getAttribute(Attributes.MAX_ABSORPTION);
                if (maxAbsorptionAttr != null) {
                    maxAbsorptionAttr.removeModifier(MAX_ABSORPTION_MODIFIER_ID);

                    AttributeModifier modifier = new AttributeModifier(
                            MAX_ABSORPTION_MODIFIER_ID,
                            10.0, // 5 hearts
                            AttributeModifier.Operation.ADD_VALUE
                    );
                    maxAbsorptionAttr.addPermanentModifier(modifier);
                }
            } else {
                var maxAbsorptionAttr = player.getAttribute(Attributes.MAX_ABSORPTION);
                if (maxAbsorptionAttr != null) {
                    maxAbsorptionAttr.removeModifier(MAX_ABSORPTION_MODIFIER_ID);
                }
            }
        }
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        // Track saturation AND food every tick
        if (!player.level().isClientSide) {
            FoodData foodData = player.getFoodData();
            SATURATION_BEFORE_EATING.put(player.getUUID(), foodData.getSaturationLevel());
            FOOD_BEFORE_EATING.put(player.getUUID(), foodData.getFoodLevel());
        }
    }

    @Override
    public void removeEffect(Player player) {
        APPLIED_LEVELS.remove(player.getUUID());
        SATURATION_BEFORE_EATING.remove(player.getUUID());
        FOOD_BEFORE_EATING.remove(player.getUUID());

        var maxAbsorptionAttr = player.getAttribute(Attributes.MAX_ABSORPTION);
        if (maxAbsorptionAttr != null) {
            maxAbsorptionAttr.removeModifier(MAX_ABSORPTION_MODIFIER_ID);
        }
    }

    public void handleFoodEaten(Player player, int foodNutrition, float foodSaturation, int level) {
        if (player.level().isClientSide) return;

        UUID playerId = player.getUUID();
        FoodData foodData = player.getFoodData();

        // Get values BEFORE eating (tracked every tick)
        float actualSaturation = SATURATION_BEFORE_EATING.getOrDefault(playerId, 0.0f);
        int actualFood = FOOD_BEFORE_EATING.getOrDefault(playerId, 0);

        // Calculate bonus percentage
        float bonusPercent = (level < 12) ? ((level - 6) * 4) / 100.0f : 0.30f;

        // Food adds: foodSaturation * 2.0
        float addedSaturation = foodSaturation * 2.0f;
        float bonusSaturation = addedSaturation * bonusPercent;

        int maxFood = 20;

        if (level >= 12 && (actualFood + foodNutrition) > maxFood) {
            // FOOD OVERFLOW!
            int foodOverflow = (actualFood + foodNutrition) - maxFood;

            // Food overflow directly converts to absorption
            float absorptionToAdd = foodOverflow;

            // Add absorption
            float currentAbsorption = player.getAbsorptionAmount();
            float newAbsorption = Math.min(currentAbsorption + absorptionToAdd, 10.0f);
            player.setAbsorptionAmount(newAbsorption);
        }

        // Always add bonus saturation
        float currentSat = foodData.getSaturationLevel();
        float newSat = Math.min(currentSat + bonusSaturation, foodData.getFoodLevel());
        foodData.setSaturation(newSat);
    }

    public int getAppliedLevel(UUID playerId) {
        return APPLIED_LEVELS.getOrDefault(playerId, 0);
    }
}