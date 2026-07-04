package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GluttonDisk extends UpgradeDisk {

    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();
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

            var maxAbsorptionAttr = player.getAttribute(Attributes.MAX_ABSORPTION);
            if (maxAbsorptionAttr != null) {
                maxAbsorptionAttr.removeModifier(MAX_ABSORPTION_MODIFIER_ID);
                if (level >= 12) {
                    maxAbsorptionAttr.addPermanentModifier(new AttributeModifier(
                            MAX_ABSORPTION_MODIFIER_ID, 10.0,
                            AttributeModifier.Operation.ADD_VALUE));
                }
            }
        }
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        if (!player.level().isClientSide) {
            FoodData foodData = player.getFoodData();
            SATURATION_BEFORE_EATING.put(player.getUUID(), foodData.getSaturationLevel());
            FOOD_BEFORE_EATING.put(player.getUUID(), foodData.getFoodLevel());
        }
    }

    @Override
    public void removeEffect(Player player) {
        UUID playerId = player.getUUID();
        APPLIED_LEVELS.remove(playerId);
        SATURATION_BEFORE_EATING.remove(playerId);
        FOOD_BEFORE_EATING.remove(playerId);

        var maxAbsorptionAttr = player.getAttribute(Attributes.MAX_ABSORPTION);
        if (maxAbsorptionAttr != null) maxAbsorptionAttr.removeModifier(MAX_ABSORPTION_MODIFIER_ID);
    }

    public void handleFoodEaten(Player player, int foodNutrition, float foodSaturation, int level) {
        if (player.level().isClientSide) return;

        UUID playerId = player.getUUID();
        FoodData foodData = player.getFoodData();

        float actualSaturation = SATURATION_BEFORE_EATING.getOrDefault(playerId, 0.0f);
        int actualFood = FOOD_BEFORE_EATING.getOrDefault(playerId, 0);

        float bonusPercent = (level < 12) ? ((level - 6) * 4) / 100.0f : 0.30f;
        float bonusSaturation = (foodSaturation * 2.0f) * bonusPercent;

        if (level >= 12 && (actualFood + foodNutrition) > 20) {
            int foodOverflow = (actualFood + foodNutrition) - 20;

            float currentAbsorption = player.getAbsorptionAmount();
            float newAbsorption = Math.min(currentAbsorption + foodOverflow, 10.0f);
            player.setAbsorptionAmount(newAbsorption);

            // Fire advancement when food overflow converts to absorption
            if (newAbsorption > currentAbsorption && player instanceof ServerPlayer sp) {
                ModAdvancementTriggers.ABSORPTION_OVERFLOW(sp);
            }
        }

        float currentSat = foodData.getSaturationLevel();
        foodData.setSaturation(Math.min(currentSat + bonusSaturation, foodData.getFoodLevel()));
    }

    public int getAppliedLevel(UUID playerId) {
        return APPLIED_LEVELS.getOrDefault(playerId, 0);
    }
}