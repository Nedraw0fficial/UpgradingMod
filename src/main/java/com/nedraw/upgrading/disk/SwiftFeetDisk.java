package com.nedraw.upgrading.disk;

import com.nedraw.upgrading.advancement.ModAdvancementTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SwiftFeetDisk extends UpgradeDisk {
    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "swift_feet_speed");
    private static final ResourceLocation SPRINT_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("upgrading", "swift_feet_sprint");

    private static final Map<UUID, Integer> APPLIED_LEVELS = new HashMap<>();
    private static final Map<UUID, Integer> SPRINT_TICKS = new HashMap<>();

    public SwiftFeetDisk() {
        super("swift_feet", "Swift Feet", DiskRarity.BASIC);
    }

    @Override
    public void applyEffect(Player player, int level, int slot, float efficiency) {
        UUID playerId = player.getUUID();
        Integer appliedLevel = APPLIED_LEVELS.get(playerId);

        if (appliedLevel == null || appliedLevel != level) {
            double speedMultiplier = (level * 3 / 100.0) * efficiency;

            var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute != null) {
                speedAttribute.removeModifier(SPEED_MODIFIER_ID);
                speedAttribute.addPermanentModifier(new AttributeModifier(
                        SPEED_MODIFIER_ID, speedMultiplier,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
            APPLIED_LEVELS.put(playerId, level);
        }
    }

    @Override
    public void applyTickEffect(Player player, int level, int slot, float efficiency) {
        if (player.level().isClientSide) return;

        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute == null) return;

        if (level >= 12) {
            if (player.isSprinting()) {
                if (!speedAttribute.hasModifier(SPRINT_MODIFIER_ID)) {
                    speedAttribute.addPermanentModifier(new AttributeModifier(
                            SPRINT_MODIFIER_ID, 0.20 * efficiency,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
                UUID id = player.getUUID();
                int ticks = SPRINT_TICKS.getOrDefault(id, 0) + 1;
                SPRINT_TICKS.put(id, ticks);
                if (ticks == 100 && player instanceof ServerPlayer sp) {
                    ModAdvancementTriggers.AERIAL_DASH(sp);
                    SPRINT_TICKS.put(id, 0);
                }
            } else {
                speedAttribute.removeModifier(SPRINT_MODIFIER_ID);
                SPRINT_TICKS.put(player.getUUID(), 0);
            }
        }
    }

    @Override
    public void removeEffect(Player player) {
        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_ID);
            speedAttribute.removeModifier(SPRINT_MODIFIER_ID);
        }
        UUID playerId = player.getUUID();
        APPLIED_LEVELS.remove(playerId);
        SPRINT_TICKS.remove(playerId);
    }
}
