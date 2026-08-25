package com.nedraw.upgrading.disk;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BerserkerDisk extends UpgradeDisk {

    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();

    private static final ResourceLocation DAMAGE_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "berserker_damage");
    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "berserker_speed");

    public BerserkerDisk() {
        super("berserker", "Berserker", DiskRarity.LEGENDARY);
    }

    @Override
    public void applyEffect(Player player, int level) {
        UUID playerId = player.getUUID();
        APPLIED_LEVELS.put(playerId, level);
    }

    @Override
    public void applyTickEffect(Player player, int level, int slot, float efficiency) {
        // Calculate health percentage
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPercent = (currentHealth / maxHealth) * 100;
        float missingHealthPercent = 100 - healthPercent;

        float damagePerTenPercent = (level - 4) / 100.0f;
        float damageBonus = (missingHealthPercent / 10.0f) * damagePerTenPercent * efficiency;

        var attackDamageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttr != null) {
            attackDamageAttr.removeModifier(DAMAGE_MODIFIER_ID);

            if (damageBonus > 0) {
                AttributeModifier damageModifier = new AttributeModifier(
                        DAMAGE_MODIFIER_ID,
                        damageBonus,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                attackDamageAttr.addTransientModifier(damageModifier);
            }
        }

        if (level >= 12 && healthPercent < 30) {

            if (attackDamageAttr != null) {
                attackDamageAttr.removeModifier(DAMAGE_MODIFIER_ID);

                float totalDamageBonus = damageBonus + (0.50f * efficiency);
                AttributeModifier damageModifier = new AttributeModifier(
                        DAMAGE_MODIFIER_ID,
                        totalDamageBonus,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                attackDamageAttr.addTransientModifier(damageModifier);
            }

            var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.removeModifier(SPEED_MODIFIER_ID);

                AttributeModifier speedModifier = new AttributeModifier(
                        SPEED_MODIFIER_ID,
                        0.30 * efficiency,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                speedAttr.addTransientModifier(speedModifier);
            }

            // Resistance II effect
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                    40,
                    1,
                    false,
                    false,
                    false
            ));
        } else {
            // Remove speed bonus if above 30%
            var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.removeModifier(SPEED_MODIFIER_ID);
            }
        }
    }

    @Override
    public void removeEffect(Player player) {
        APPLIED_LEVELS.remove(player.getUUID());

        var attackDamageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttr != null) {
            attackDamageAttr.removeModifier(DAMAGE_MODIFIER_ID);
        }

        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_MODIFIER_ID);
        }
    }

    public int getAppliedLevel(UUID playerId) {
        return APPLIED_LEVELS.getOrDefault(playerId, 0);
    }
}