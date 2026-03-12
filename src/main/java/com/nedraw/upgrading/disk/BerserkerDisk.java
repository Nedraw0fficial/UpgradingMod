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
    public void applyTickEffect(Player player, int level) {
        // Calculate health percentage
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPercent = (currentHealth / maxHealth) * 100;
        float missingHealthPercent = 100 - healthPercent;

        // Calculate damage bonus based on missing health
        // Level 9: 5% per 10% missing, Level 10: 6%, Level 11: 7%, Level 12: 8%
        float damagePerTenPercent = (level - 4) / 100.0f; // 9-4=5%, 10-4=6%, etc
        float damageBonus = (missingHealthPercent / 10.0f) * damagePerTenPercent;

        // Apply damage modifier
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

        // Level 12: Below 30% HP bonuses
        if (level >= 12 && healthPercent < 30) {
            // Additional +50% damage
            if (attackDamageAttr != null) {
                attackDamageAttr.removeModifier(DAMAGE_MODIFIER_ID);

                float totalDamageBonus = damageBonus + 0.50f; // Add 50% extra
                AttributeModifier damageModifier = new AttributeModifier(
                        DAMAGE_MODIFIER_ID,
                        totalDamageBonus,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                attackDamageAttr.addTransientModifier(damageModifier);
            }

            // +30% movement speed
            var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.removeModifier(SPEED_MODIFIER_ID);

                AttributeModifier speedModifier = new AttributeModifier(
                        SPEED_MODIFIER_ID,
                        0.30,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                speedAttr.addTransientModifier(speedModifier);
            }

            // Resistance II effect
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,
                    40, // 2 seconds (20 ticks * 2)
                    1,  // Level II (0-indexed, so 1 = level 2)
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