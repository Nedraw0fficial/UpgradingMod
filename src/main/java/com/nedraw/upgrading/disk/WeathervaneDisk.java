package com.nedraw.upgrading.disk;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WeathervaneDisk extends UpgradeDisk {

    private static final ResourceLocation DAMAGE_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "weathervane_damage");
    private static final ResourceLocation SPRINT_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "weathervane_sprint");

    private static final double[] DAMAGE_BONUS = {0, 0, 0, 0.02, 0.03, 0.05, 0.08, 0.10, 0.13, 0.16, 0.20, 0.28};
    private static final double[] SPEED_BONUS  = {0, 0, 0, 0.05, 0.07, 0.09, 0.12, 0.15, 0.18, 0.20, 0.21, 0.25};

    private static final Map<UUID, Boolean> LAST_WEATHER = new HashMap<>();

    public WeathervaneDisk() {
        super("weathervane", "Weathervane", DiskRarity.RARE);
    }

    @Override
    public void applyEffect(Player player, int level, int slot, float efficiency) {
        updateModifiers(player, level, efficiency);
    }

    @Override
    public void applyTickEffect(Player player, int level, int slot, float efficiency) {
        boolean isThundering = player.level().isThundering();
        boolean isRaining = player.level().isRaining();
        boolean weatherActive = level >= 12 ? isRaining : isThundering;

        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        if (weatherActive && player.isSprinting()) {
            if (!speedAttr.hasModifier(SPRINT_MODIFIER_ID)) {
                double bonus = SPEED_BONUS[Math.min(level - 1, SPEED_BONUS.length - 1)] * efficiency;
                speedAttr.addPermanentModifier(new AttributeModifier(
                        SPRINT_MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        } else {
            speedAttr.removeModifier(SPRINT_MODIFIER_ID);
        }
    }

    private void updateModifiers(Player player, int level, float efficiency) {
        boolean isThundering = player.level().isThundering();
        boolean isRaining = player.level().isRaining();
        boolean active = level >= 12 ? isRaining : isThundering;

        var damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (damageAttr != null) {
            damageAttr.removeModifier(DAMAGE_MODIFIER_ID);
            if (active && level >= 4) {
                double bonus = DAMAGE_BONUS[Math.min(level - 1, DAMAGE_BONUS.length - 1)] * efficiency;
                damageAttr.addPermanentModifier(new AttributeModifier(
                        DAMAGE_MODIFIER_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
    }

    @Override
    public void removeEffect(Player player) {
        var damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (damageAttr != null) damageAttr.removeModifier(DAMAGE_MODIFIER_ID);
        if (speedAttr != null) speedAttr.removeModifier(SPRINT_MODIFIER_ID);
        LAST_WEATHER.remove(player.getUUID());
    }
}
