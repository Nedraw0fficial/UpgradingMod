package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TankyDisk extends UpgradeDisk {

    private static final ResourceLocation HEALTH_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "tanky_health");
    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "tanky_speed");

    // Track players who already received the advancement to avoid spam
    private static final Set<UUID> ADVANCEMENT_FIRED = new HashSet<>();

    public TankyDisk() {
        super("tanky", "Tanky", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level) {
        if (level < 12) {
            applyPermanentModifiers(player, level);

            // Fire advancement when health boost reaches 30% (level 12 only,
            // but also check if player somehow has high boost from level progression)
        }

        // Check if this level gives >= 30% health boost
        if (getHealthBoost(level) >= 0.30f || level >= 12) {
            if (!ADVANCEMENT_FIRED.contains(player.getUUID()) && player instanceof ServerPlayer sp) {
                ModAdvancementTriggers.TANKY_30_PERCENT(sp);
                ADVANCEMENT_FIRED.add(player.getUUID());
            }
        }
    }

    @Override
    public void applyTickEffect(Player player, int level) {
        if (level >= 12) {
            applyLevel12Effect(player);
        }
    }

    @Override
    public void removeEffect(Player player) {
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (healthAttr != null) healthAttr.removeModifier(HEALTH_MODIFIER_ID);
        if (speedAttr != null) speedAttr.removeModifier(SPEED_MODIFIER_ID);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }

        ADVANCEMENT_FIRED.remove(player.getUUID());
    }

    private void applyPermanentModifiers(Player player, int level) {
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);

        float healthBoost = getHealthBoost(level);
        float speedReduction = getSpeedReduction(level);

        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_MODIFIER_ID);
            healthAttr.addPermanentModifier(new AttributeModifier(
                    HEALTH_MODIFIER_ID, healthBoost,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }

        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_MODIFIER_ID);
            speedAttr.addPermanentModifier(new AttributeModifier(
                    SPEED_MODIFIER_ID, -speedReduction,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }

    private void applyLevel12Effect(Player player) {
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);

        if (healthAttr != null && !healthAttr.hasModifier(HEALTH_MODIFIER_ID)) {
            healthAttr.addPermanentModifier(new AttributeModifier(
                    HEALTH_MODIFIER_ID, 0.30,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }

        if (speedAttr != null) {
            boolean isSprinting = player.isSprinting();
            boolean hasSpeedMod = speedAttr.hasModifier(SPEED_MODIFIER_ID);

            if (isSprinting && !hasSpeedMod) {
                speedAttr.addPermanentModifier(new AttributeModifier(
                        SPEED_MODIFIER_ID, -0.21,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            } else if (!isSprinting && hasSpeedMod) {
                speedAttr.removeModifier(SPEED_MODIFIER_ID);
            }
        }
    }

    private float getHealthBoost(int level) {
        return switch (level) {
            case 1  -> 0.05f;  case 2  -> 0.06f;  case 3  -> 0.07f;
            case 4  -> 0.10f;  case 5  -> 0.12f;  case 6  -> 0.14f;
            case 7  -> 0.16f;  case 8  -> 0.20f;  case 9  -> 0.22f;
            case 10 -> 0.24f;  case 11 -> 0.26f;
            default -> 0.05f;
        };
    }

    private float getSpeedReduction(int level) {
        return switch (level) {
            case 1  -> 0.35f;  case 2  -> 0.32f;  case 3  -> 0.29f;
            case 4  -> 0.26f;  case 5  -> 0.24f;  case 6  -> 0.22f;
            case 7  -> 0.21f;  case 8  -> 0.20f;  case 9  -> 0.19f;
            case 10 -> 0.17f;  case 11 -> 0.15f;
            default -> 0.35f;
        };
    }
}